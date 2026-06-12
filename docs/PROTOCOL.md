# erxes Messenger — Backend Protocol Reference

This is the **source of truth** for the network contract. It is reverse-engineered
from the official [erxes-ios-sdk](https://github.com/erxes/erxes-ios-sdk) and the
erxes React Native widget. The Android SDK must speak exactly this protocol.

---

## 1. Endpoints

All URLs derive from a single configured `endpoint` (base URL, e.g.
`https://app.example.io`). Strip a trailing `/` before appending.

| Purpose       | Method | URL                                                              | Content-Type                |
|---------------|--------|------------------------------------------------------------------|-----------------------------|
| GraphQL       | POST   | `{base}/gateway/graphql`                                         | `application/json`          |
| Subscriptions | WS     | `{base}/gateway/graphql` (ws/wss), `graphql-ws` protocol         | —                           |
| File upload   | POST   | `{base}/gateway/upload-file?kind=main&maxHeight=0&maxWidth=0`    | `multipart/form-data`       |

- `fileEndpoint` defaults to `endpoint`; override only when the file server differs.
  Note: in the iOS SDK GraphQL actually runs on `fileEndpoint` (no `w.` subdomain) —
  the `w.` host returns 405. Keep them equal unless told otherwise.
- GraphQL request body: `{ "query": "<op>", "variables": { ... } }`.
- A GraphQL response may contain a top-level `errors` array even with HTTP 200 — always check it.

### File upload specifics
- Multipart field name is `file`; include `filename` and per-part `Content-Type`.
- Allowed MIME types: `image/png`, `image/jpeg` only.
- **Response body is the plain-text file key** (not JSON). Use it as the attachment `url`.

---

## 2. Identity & session

Persisted across launches (iOS `UserDefaults` → Android `DataStore`/SharedPreferences):

| Key                       | Meaning                                                        |
|---------------------------|----------------------------------------------------------------|
| `cachedCustomerId`        | Set after a successful `connect`; re-identifies the customer.   |
| `visitorId`               | Generated **once** (Mongo-style ObjectId), reused forever.      |
| `conversationId`          | Last active conversation.                                       |
| `integrationId`           | The integration the identity is bound to.                      |
| `identified`              | True once visitor supplied email/phone (requireAuth).          |

**Integration binding rule:** identity is meaningless across integrations. If the
configured `integrationId` differs from the persisted one, **clear** `cachedCustomerId`,
`visitorId`, `conversationId`, and `identified` before use.

**visitorId generation:** Mongo ObjectId = 24 hex chars = 4-byte epoch seconds +
5-byte random + 3-byte counter. Generate one if none persisted.

---

## 3. Connect handshake (run on every launch)

### Step 1 — `widgetsMessengerConnect` (mutation)

Variables: `integrationId` (required), `visitorId`, `cachedCustomerId`, `email`,
`phone`, `isUser`, `code`, `data` (JSON), `companyData` (JSON).

Returns (top-level fields used by the client):
- `integrationId`, `customerId`, `visitorId`, `languageCode`
- `uiOptions` (JSON) — `primary.DEFAULT` hex color, `backgroundColor`, `textColor`, `wallpaper`, `logo`
- `messengerData` (JSON) — see shape below
- `ticketConfig` — **top-level**, not inside messengerData: `_id`, `name`, `pipelineId`,
  `channelId`, `selectedStatusId`, `parentId`, `formFields{ name, description, attachment, tags }`
  where each field cfg = `{ isShow, label, placeholder, order }`.

`messengerData` shape:
```
supporterIds: [String], notifyCustomer: Bool, availabilityMethod: String,
isOnline: Bool, onlineHours: [{ day, from, to }], timezone: String,
messages: { greetings: { title, message }, away, thank, welcome },
links: { facebook, instagram, twitter|x, youtube, linkedin, discord, github },
knowledgeBaseTopicId: String, websiteApps: [{ _id, kind, credentials{ url, buttonText, description }, showInInbox }],
responseRate: String, requireAuth: Bool, showChat: Bool, showLauncher: Bool,
forceLogoutWhenResolve: Bool, showVideoCallRequest: Bool
```

After connect: persist `customerId` → `cachedCustomerId`.

### Step 2 — fire-and-forget (non-blocking)
- `widgetsSaveBrowserInfo(customerId, visitorId, browserInfo: JSON!)` — may trigger auto welcome messages.
- `widgetsMessengerSupporters(integrationId)` — supporters list + online state for the launcher.

---

## 4. GraphQL operations

### Queries
| Operation                       | Variables                                   | Returns |
|---------------------------------|---------------------------------------------|---------|
| `widgetsConversations`          | integrationId!, customerId, visitorId       | conversation list w/ last messages, unreadCount |
| `widgetsConversationDetail`     | _id!, integrationId                          | one conversation, all messages, supporters, online |
| `widgetsMessengerSupporters`    | integrationId!                               | supporters[], isOnline, availabilityMethod |
| `widgetsTotalUnreadCount`       | integrationId!, customerId, visitorId       | Int badge count |

### Mutations
| Operation                          | Notes |
|------------------------------------|-------|
| `widgetsMessengerConnect`          | Handshake (above). |
| `widgetsSaveBrowserInfo`           | Page-visit / auto messages. |
| `widgetsInsertMessage`             | Send a chat message. Vars: integrationId!, customerId, visitorId, conversationId, message, contentType, attachments:[AttachmentInput]. Returns `MessageFields`. |
| `widgetsReadConversationMessages`  | conversationId! → mark read. |
| `editCustomer` / `widgetsTicketCustomersEdit` | Update name/email/phone; returns `_id` to persist as cachedCustomerId. |
| `widgetsSaveCustomerGetNotified`   | customerId/visitorId, type!, value! — email/phone opt-in. |
| `widgetsLeadConnect` / `widgetsSaveLead` | Lead/form widget. |

### `MessageFields` (shared message fragment)
`_id, conversationId, customerId, user{ _id, details{ avatar, fullName } }, content,
createdAt, internal, fromBot, contentType, engageData{ content, kind, sentAs, fromUser{...} },
botData, messengerAppData, attachments{ url, name, size, type }`

---

## 5. Subscriptions (`graphql-ws` over WebSocket)

Connect to `wss://{base}/gateway/graphql` (derive from `endpoint`, not fileEndpoint:
`https://`→`wss://`, `http://`→`ws://`) with subprotocol `graphql-transport-ws`.
Lifecycle (confirmed against the iOS `ChatViewModel`): `connection_init` →
`connection_ack` → `subscribe`(id, payload{query,variables}) → `next` data frames →
`error` / `complete`. To stop, send `{id, type:"complete"}`. Reconnect on drop with
exponential backoff (1s → 30s cap). Dedup inbound messages by `_id` (the subscription
echoes your own sent messages back).

| Subscription                         | Variable        | Payload |
|--------------------------------------|-----------------|---------|
| `conversationMessageInserted`        | _id! (convId)   | new message (full message fields) |
| `conversationBotTypingStatus`        | _id! (convId)   | `{ _id, typing }` |
| `conversationClientMessageInserted`  | userId!         | echo of customer's own message (dedup) |
| `conversationAdminMessageInserted`   | customerId!     | `{ customerId, unreadCount }` |
| `conversationChanged`                | conversationId! | `{ conversationId, type }` status change |

> Note: the iOS `NetworkClient` (Apollo) skips WS, but `ChatViewModel` implements the
> subscription directly over `URLSessionWebSocketTask` with `graphql-transport-ws` —
> that is the authoritative framing above. The Android port mirrors it on OkHttp's
> WebSocket (`network/RealtimeClient.kt`), exposed as cold `Flow`s.

---

## 6. References
- iOS SDK source: `Sources/MessengerSDK/Network/Operations/*.graphql`
- Connect parsing: `Messenger/ViewModels/AppViewModel.swift`
- Upload: `Network/Upload/FileUploader.swift`
- Session: `Session/SessionManager.swift`

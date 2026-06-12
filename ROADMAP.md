# erxes Android Messenger SDK — Roadmap

Native Android port of [erxes-ios-sdk](https://github.com/erxes/erxes-ios-sdk).
Tracks work phase by phase. Tick boxes as each lands. See `docs/` for the contract,
architecture, and iOS→Android file map.

Legend: ☐ todo · ◐ in progress · ☑ done

---

## Phase 0 — Project scaffolding
- ☑ Gradle project: `messenger-sdk` library module + `app` sample module
- ☑ Kotlin, Compose, AGP, version catalog (`libs.versions.toml`) configured
- ☑ Dependencies: Apollo Kotlin, Coroutines, DataStore, Coil, Lifecycle/Compose
- ☑ `git init`, `.gitignore`, base package `com.erxes.messenger`
- ☑ Gradle wrapper 8.11.1; `./gradlew :messenger-sdk:assembleDebug :app:assembleDebug` green
- ☐ CI stub (build + lint)

> Phase 0 done: AAR + sample APK build successfully. Skeleton public API
> (`ErxesMessenger`, `MessengerConfig`, `MessengerUser`, `ObjectId`) in place;
> connect/UI/realtime are stubbed with `TODO(Phase N)` markers.

## Phase 1 — Core networking & session (no UI)
- ☑ `MessengerConfig` + `Appearance` (endpoint, integrationId, fileEndpoint, cachedCustomerId)
- ☑ `ObjectId` generator (Mongo 24-hex) → `visitorId`
- ☑ `SessionStore` (DataStore): cachedCustomerId, visitorId, conversationId, integration binding reset, identified
- ☑ `GraphQLClient`: OkHttp + kotlinx.serialization, `{base}/gateway/graphql`, error handling
- ☑ GraphQL operations ported from iOS (raw strings in `MessengerOperations`; Apollo codegen deferred to Phase 3)
- ☑ **Connect handshake**: `widgetsMessengerConnect` → parse uiOptions/messengerData, persist customerId
- ☑ `widgetsSaveBrowserInfo` + `widgetsMessengerSupporters` (fire-and-forget)
- ☑ Unit tests: ObjectId, ConnectParser (real-shape JSON), GraphQLClient (MockWebServer) — 10 tests green
- ☐ Verify connect against a real/staging integration (needs endpoint + integrationId)
- ◐ ticketConfig + websiteApps parsing deferred to Phase 6 (their feature phase)

> Phase 1 done (pending live verification): `ErxesMessenger.configure()` runs the
> handshake on a background scope, flips `isReady`, exposes `connectResponse`/`connectError`
> StateFlows, and persists `cachedCustomerId`. Chose raw HTTP+JSON over Apollo codegen
> because codegen needs a reachable schema; will migrate in Phase 3 for subscriptions.

## Phase 2 — Conversations & messaging (data layer)
- ☑ Models: `Conversation`, `Message`, `Attachment`, `MessageUser`, `UserDetails`, `ParticipatedUser`
- ☑ `widgetsConversations` (list) + `widgetsConversationDetail` (thread)
- ☑ `widgetsInsertMessage` (send) — persists assigned conversationId
- ☑ `widgetsReadConversationMessages` + `widgetsTotalUnreadCount`
- ☑ `MessageParser` (pure) + `DateParsing` (epoch/ISO, no desugaring)
- ☑ Repository methods: conversations/detail/send/markRead/totalUnread
- ☑ Unit tests: MessageParser (customer vs agent, attachments, unread derivation) — 14 total green
- ☐ Optimistic local echo + `StateFlow` exposure (lands with the ChatViewModel in Phase 5)
- ☐ Verify against a live integration

## Phase 3 — Realtime (WebSocket subscriptions)
- ☐ Apollo `graphql-ws` transport on `{base}/gateway/graphql`
- ☐ `conversationMessageInserted` → live append
- ☐ `conversationBotTypingStatus` → typing indicator
- ☐ `conversationClientMessageInserted` (dedup), `conversationAdminMessageInserted` (badge), `conversationChanged`
- ☐ Reconnect/backoff handling

## Phase 4 — File upload & attachments
- ☐ `FileUploader`: multipart POST to `/gateway/upload-file`, PNG/JPEG guard, plain-text key response
- ☐ Image picker integration in sample + chat
- ☐ Attachment URL resolution (`AttachmentUrl` util)

## Phase 5 — UI (Compose, parity with SwiftUI)
- ☐ `MessengerLauncher` draggable button, snaps to corners; shows after `isReady`
- ☐ `LauncherOverlay` window/host
- ☐ Home / Conversation list / Chat screens
- ☐ Components: message bubble, avatar+status, typing, date separators, attachments, welcome
- ☐ Theming from `uiOptions` (primary color, background, logo, wallpaper)
- ☐ Keyboard handling / scroll-to-bottom

## Phase 6 — Auxiliary features
- ☐ requireAuth identity form (`editCustomer` / `widgetsTicketCustomersEdit`)
- ☐ Get-notified opt-in (`widgetsSaveCustomerGetNotified`)
- ☐ Knowledge base / Help (article browse + search)
- ☐ Tickets (create + list) using `ticketConfig`
- ☐ Lead/form widget (`widgetsLeadConnect` / `widgetsSaveLead`)
- ☐ Social links footer

## Phase 7 — Packaging & docs
- ☐ Publish AAR to Maven Local / a Maven repo (group `com.erxes`, artifact `messenger-sdk`)
- ☐ Consumer ProGuard/R8 rules
- ☐ README integration guide finalized
- ☐ Sample app demonstrating every feature
- ☐ Version `0.1.0` tag

---

### Milestones
1. **M1 — "It connects":** Phase 0–1. Headless connect succeeds, identity persists.
2. **M2 — "It chats":** Phase 2–3. Send/receive messages live.
3. **M3 — "It ships":** Phase 4–7. Full UI, upload, packaged AAR.

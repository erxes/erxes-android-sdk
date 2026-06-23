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
- ☑ `graphql-transport-ws` transport on `wss://{base}/gateway/graphql` (OkHttp WebSocket, not Apollo)
- ☑ `conversationMessageInserted` → `messageStream(conversationId): Flow<Message>`
- ☑ `conversationBotTypingStatus` → `botTypingStream(conversationId): Flow<Boolean>`
- ☑ Reconnect/backoff handling (1s → 30s cap via `retryWhen`)
- ☑ Unit test: full init→ack→subscribe→next lifecycle via MockWebServer — 16 total green
- ◐ Dedup-by-id + mark-read-on-inbound handled at the ChatViewModel layer (Phase 5)
- ☐ `conversationClientMessageInserted` / `conversationAdminMessageInserted` (badge) / `conversationChanged` (deferred; not needed until multi-conversation UI)
- ☐ Verify against a live integration

> Phase 3 done: confirmed the iOS framing is `graphql-transport-ws` (ChatViewModel
> implements it directly; only the Apollo NetworkClient was stubbed). Stayed on OkHttp
> WebSocket rather than introducing Apollo codegen — same rationale as Phase 1 (no
> reachable schema), and OkHttp keeps the dependency surface small.

## Phase 4 — File upload & attachments
- ☑ `FileUploader`: multipart POST to `/gateway/upload-file`, PNG/JPEG guard, plain-text key response
- ☑ `UploadedAttachment` + `toAttachment()` bridge into `sendMessage`
- ☑ Repository `uploadAttachment()`; shared `Call.await()` extracted to `HttpExt`
- ☑ Unit tests: url build, multipart shape, plain-text key, mime/empty rejections — 20 total green
- ☐ Image picker integration in sample + chat (lands with Phase 5 UI)
- ☐ Attachment URL resolution (`AttachmentUrl` util — for rendering remote attachment urls)

## Phase 5 — UI (Compose, parity with SwiftUI)
### 5a — Chat experience ☑
- ☑ `MessengerTheme` from `uiOptions.color` (hex parse) + appearance fallback
- ☑ `ChatViewModel`: load detail, send (optimistic id adoption for new conv), realtime
      append (dedup-by-id) + mark-read-on-inbound, bot typing
- ☑ `ChatScreen` (LazyColumn, input bar, ImeAction.Send, scroll-to-bottom, imePadding)
- ☑ Components: `MessageBubble` (l/r align, attachments via Coil), `Avatar`, `TypingIndicator`
- ☑ `AttachmentUrl.resolve` (read-file gateway) ported
- ☑ `MessengerActivity` hosts chat; `ErxesMessenger.show()` launches it
### 5b — Launcher & list ☑
- ☑ `MessengerLauncher` draggable button, snaps to nearest edge; shows after `isReady`
- ☑ Compose `ErxesMessengerHost` overlay (used in the sample)
- ☑ `MessengerRoot` in-activity nav (Home → List → Chat) with a small back stack
- ☑ `HomeScreen` (branded welcome header from greetings), `ConversationListScreen` (unread badges)
- ◐ Date separators, supporters/online status (deferred — nice-to-have polish)
### 5c — Chat display mode (AI-assistant shell) ☑
- ☑ `DisplayMode { CLASSIC, CHAT }` on `MessengerConfig` (default CLASSIC)
- ☑ `ChatModeScreen`: new-chat home (greeting + composer), left drawer (conversation
      list + New chat), inline chats; reuses `ChatContent` + `ConversationListViewModel`
- ☑ `ChatContent` extracted from `ChatScreen` so both shells share the message list/composer
- ☑ requireAuth gate before starting a new chat (reuses `IdentityFormScreen`)
- ☑ `MessengerActivity` routes to `ChatModeScreen` in CHAT mode; launcher suppressed in CHAT
- ☑ Image attachments in the composer (`ComposerBar` "+" → Photo Picker → JPEG upload →
      staged thumbnail strip → send), shared by classic + chat shells; speech-to-text omitted
- ☑ Theme parity with iOS `effective*`: primary from `uiOptions.color`, background/surface
      from `uiOptions.backgroundColor`, light/dark scheme derived from background luminance
- ☑ Clean chat-mode chrome: center-aligned transparent top bar, greeting hero, pill composer
- ☐ `homeActions`/`drawerActions` + `onAction` callback (deferred follow-up)
- ☐ Auto-present on connect (chose `show()`-driven open — idiomatic Android)

## Phase 6 — Auxiliary features
### 6a — Identity, notifications, social ☑
- ☑ requireAuth identity form (`widgetsTicketCustomersEdit`) — gates new conversations in `MessengerRoot`
- ☑ Get-notified opt-in (`widgetsSaveCustomerGetNotified`) — repository method
- ☑ `ErxesMessenger.isIdentified`/`requireAuth` + `identify()`/`saveGetNotified()` public API
- ☑ Social links footer on Home (opens links in browser)
### 6b — Tickets ☑
- ☑ ticketConfig parsing in ConnectParser (top-level of connect; formFields)
- ☑ Models: Ticket, TicketStatus, TicketAssignee, TicketTag, TicketConfig
- ☑ Operations: widgetTicketsByCustomer, widgetTicketCreated, widgetsGetTicketTags
- ☑ Repository: tickets(), createTicket(), ticketTags()
- ☑ TicketsScreen (list + status chips) + CreateTicketScreen (form from formFields + tags)
- ☑ Home shows "Support tickets" only when ticketConfig present
- ☑ Unit tests: ticketConfig parse (+null guard), ticket parse — 23 total green
### 6c — Knowledge base ☑
- ☑ Models: KbTopic, KbCategory, KbArticle
- ☑ Operation: cpKnowledgeBaseTopicDetail (uses messengerData.knowledgeBaseTopicId)
- ☑ KbParser (pure) incl. articlesFor() child-category merge + dedup
- ☑ Repository: knowledgeBase(topicId)
- ☑ HelpScreen: categories → articles → article detail (HTML via TextView/HtmlCompat)
- ☑ Home shows "Browse help articles" only when knowledgeBaseTopicId present
- ☑ Unit tests: topic parse, articlesFor merge/dedup — 25 total green
- ☐ Article full-text search (deferred polish)
- ☐ Lead/form widget (`widgetsLeadConnect` / `widgetsSaveLead`) — iOS has no UI flow; deferred
- ☐ websiteApps parsing (deferred — secondary)

## Phase 7 — Packaging & docs
- ☑ Maven publishing (`maven-publish`): `com.erxes:messenger-sdk:0.30.0`, release variant + sources jar + POM
- ☑ `publishToMavenLocal` verified (AAR/sources/POM/module land in ~/.m2)
- ☑ Consumer ProGuard/R8 rules (keep public API + MessengerActivity)
- ☑ CI workflow (`.github/workflows/build.yml`): unit tests + assemble on push/PR
- ☑ README install snippet uses `0.30.0`
- ◐ Remote Maven publishing (Maven Central / private repo) — add credentials + repository block when needed
- ☐ Verify against a live integration before a public release

## Phase 8 — Chat-mode parity with iOS
Bring `DisplayMode.CHAT` to parity with the current iOS chat-mode shell
(`MessengerChatModeView` + `ChatContentView`). Chat mode only; classic shell untouched.
**Speech-to-text dictation is intentionally excluded.** `persistentMenu` is dropped —
it is dead code on iOS (declared, never populated).

### 8a — Message list fidelity
- ☑ `util/MessageGrouper.kt` (pure): `ChatRow` (date separator | message + isFirst/isLast),
      5-min same-sender grouping, Today/Yesterday/medium-date labels
- ☑ `ChatViewModel`: expose `chatRows` + `isBot` on `ChatUiState`, rebuilt on message change
- ☑ `ui/components/DateSeparator.kt` + `WelcomeMessage.kt` (welcome from `messages.welcome`)
- ☑ `MessageBubble` grouped corner radii (isFirst/isLast); avatar only on group's last message
- ☑ `ChatContent` renders `chatRows` (separator vs. bubble) instead of a flat list
- ☑ Unit test: `MessageGrouperTest` (window, day-change separators, attachment-only retention)

### 8b — Chat title (agent / bot / online)
- ☑ `ui/components/AvatarWithStatus.kt` (online green dot overlay over `Avatar`)
- ☑ `ui/components/ChatTitle.kt`: agent (avatar + Online/Offline) / "AI agent" badge / fallback
- ☑ `ChatModeScreen` top bar shows `ChatTitle` for the active conversation, "New chat" otherwise

### 8c — Scroll affordances + timestamps
- ☑ Scroll-to-bottom FAB driven by `LazyListState`
- ☑ Swipe-to-reveal timestamp column (fallback: long-press timestamp if it fights drawer swipe)

### 8d — Long-press copy
- ☑ `MessageBubble` long-press → copy to clipboard → transient "Copied" toast

### 8e — Host actions API
- ☑ `config/ActionItem.kt` (id, title, icon as `ImageVector`/`@DrawableRes`)
- ☑ `MessengerConfig.homeActions`/`drawerActions`; `ErxesMessenger.onAction: ((String)->Unit)?`
- ☑ `ChatModeScreen`: home-only top-bar action icons + drawer action rows, both fire `onAction`

### 8f — Drawer polish
- ☑ Rich drawer rows (avatar + last-message preview + active highlight)
- ☑ Floating "New chat" pill (iOS style)
- ☑ Confirm ChatViewModel instances survive conversation switches

### 8g — (Optional) Audio attachment playback
- ☐ Port `AudioMessageView` (MediaPlayer/ExoPlayer) for audio-type attachments

---

### Milestones
1. **M1 — "It connects":** Phase 0–1. Headless connect succeeds, identity persists.
2. **M2 — "It chats":** Phase 2–3. Send/receive messages live.
3. **M3 — "It ships":** Phase 4–7. Full UI, upload, packaged AAR.

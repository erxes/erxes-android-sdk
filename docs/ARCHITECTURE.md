# Android SDK — Architecture

Native Android port of the erxes iOS Messenger SDK. Goal: feature parity with a
Kotlin/Compose public API that feels idiomatic to Android hosts.

## Tech stack

| Concern        | Choice                          | Mirrors (iOS)              |
|----------------|---------------------------------|----------------------------|
| Language       | Kotlin                          | Swift                      |
| UI             | Jetpack Compose                 | SwiftUI                    |
| Async/streams  | Coroutines + Flow / StateFlow   | async-await + Combine      |
| GraphQL + WS   | Apollo Kotlin (`com.apollographql.apollo`) | Apollo iOS      |
| HTTP/upload    | OkHttp (Apollo's engine)        | URLSession                 |
| Persistence    | Jetpack DataStore (Preferences) | UserDefaults               |
| Image loading  | Coil                            | SDWebImageSwiftUI          |
| Min SDK        | 24 (Android 7.0)                | iOS 16                     |

> Alternative to Apollo: OkHttp + kotlinx.serialization with hand-written queries,
> matching the iOS SDK's raw-URLSession approach. Apollo is recommended because it
> gives typed models **and** a working `graphql-ws` subscription transport for free.

## Module layout

```
android-sdk/
├── messenger-sdk/            # the publishable Android library (AAR)
│   └── src/main/kotlin/com/erxes/messenger/
│       ├── ErxesMessenger.kt         # public entry point (configure/setUser/show…)
│       ├── config/MessengerConfig.kt
│       ├── config/Appearance.kt
│       ├── network/
│       │   ├── ApolloProvider.kt      # builds ApolloClient from endpoint
│       │   ├── GraphQLClient.kt       # send/object/array helpers + error handling
│       │   └── FileUploader.kt        # multipart upload-file
│       ├── session/SessionStore.kt    # DataStore-backed identity persistence
│       ├── session/ObjectId.kt        # Mongo ObjectId generator (visitorId)
│       ├── data/
│       │   ├── model/                 # ConnectResponse, Conversation, Message, Supporter, Ticket…
│       │   └── repository/MessengerRepository.kt
│       └── ui/
│           ├── MessengerViewModel.kt  # ≈ AppViewModel
│           ├── launcher/              # draggable launch button + overlay
│           ├── conversation/          # list + chat screens
│           └── components/            # bubbles, avatars, typing, attachments
├── app/                      # sample host application
├── docs/
├── ROADMAP.md
└── CLAUDE.md
```

## Layers (dependency direction: UI → repository → network/session)

1. **Public API** (`ErxesMessenger`) — singleton facade. `configure()`, `setUser()`,
   `clearUser()`, `show(activity)`, plus a Compose `MessengerLauncher()` and `ErxesMessengerHost()`.
2. **Repository** — orchestrates the connect handshake, conversations, messages,
   subscriptions; exposes `StateFlow`s the UI observes. Equivalent of `AppViewModel`/`ChatViewModel`.
3. **Network** — Apollo client + multipart uploader. URL construction per `docs/PROTOCOL.md`.
4. **Session** — DataStore identity store + ObjectId. Enforces the integration-binding reset rule.

## Public API sketch

```kotlin
ErxesMessenger.configure(
    context,
    MessengerConfig(
        endpoint = "https://app.example.io",
        integrationId = "YOUR_INTEGRATION_ID",
    ),
)
ErxesMessenger.setUser(MessengerUser(email = "jane@x.io", name = "Jane Doe"))
ErxesMessenger.show(activity)            // imperative open

// Compose host:
ErxesMessengerHost {                     // provides overlay + launcher
    MessengerLauncher()                  // draggable button, snaps to corners
}
```

## Threading & state
- All network on `Dispatchers.IO`; UI state hoisted to `StateFlow` collected with
  `collectAsStateWithLifecycle`.
- `isReady` flips true after the connect handshake succeeds — launcher shows only then.

See `docs/PROTOCOL.md` for the exact wire contract and `docs/PORTING-MAP.md` for the
file-by-file iOS→Android correspondence.

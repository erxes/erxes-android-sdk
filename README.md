# erxes Android Messenger SDK

Native Android SDK that embeds a fully-featured erxes customer messenger — real-time
chat, knowledge base, and support tickets — into your Android app. Kotlin-first port
of the [erxes iOS SDK](https://github.com/erxes/erxes-ios-sdk).

> **Status: in development.** See [`ROADMAP.md`](ROADMAP.md) for what works today.

## Features (target parity)
- 🔴 Real-time chat over WebSocket (`graphql-ws`) with auto-reconnect
- 🤖 Bot conversations with typing indicators
- 🎈 Draggable launcher button that snaps to screen corners
- 📎 Image upload (PNG/JPEG) with progress
- 📚 Knowledge base browsing & search
- 🎫 Support ticket creation & tracking
- 👤 Optional user identification

## Requirements
- Android 7.0 (API 24)+
- Kotlin · Jetpack Compose
- An erxes backend endpoint + integration ID (Dashboard → Settings → Integrations)

## Install
```kotlin
// settings.gradle.kts → repositories { mavenCentral(); mavenLocal() }
dependencies {
    implementation("com.erxes:messenger-sdk:0.30.0")
}
```

## Quick start
```kotlin
// Application.onCreate()
ErxesMessenger.configure(
    context = this,
    config = MessengerConfig(
        endpoint = "https://your.erxes.instance",
        integrationId = "YOUR_INTEGRATION_ID",
    ),
)

// Optional — identify the signed-in user
ErxesMessenger.setUser(MessengerUser(email = "jane@example.com", name = "Jane Doe"))
ErxesMessenger.clearUser() // on logout
```

Open the messenger:
```kotlin
// Imperative
ErxesMessenger.show(activity)

// Or in Compose
ErxesMessengerHost {
    MessengerLauncher()   // floating draggable button
}
```

## Documentation
| Doc | What |
|-----|------|
| [`docs/PROTOCOL.md`](docs/PROTOCOL.md) | Backend contract: endpoints, GraphQL ops, WebSocket flow |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Module layout, tech stack, public API |
| [`docs/PORTING-MAP.md`](docs/PORTING-MAP.md) | iOS → Android file mapping |
| [`ROADMAP.md`](ROADMAP.md) | Phased implementation plan |

## License
AGPLv3 — matching the upstream erxes SDK.

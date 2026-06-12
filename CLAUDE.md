# CLAUDE.md

Guidance for Claude Code working in this repository.

## What this is
A **native Android port** of the erxes iOS Messenger SDK
(<https://github.com/erxes/erxes-ios-sdk>). It embeds a real-time customer messenger
(chat, knowledge base, tickets) into a host Android app, talking to an erxes backend
over GraphQL + WebSocket.

The iOS source is the reference implementation. A read-only clone lives at
`/tmp/erxes-ios-sdk` (re-clone with `git clone --depth 1 https://github.com/erxes/erxes-ios-sdk`).

## Read these first
- `docs/PROTOCOL.md` — the backend wire contract (endpoints, GraphQL ops, WS flow). **Source of truth.**
- `docs/ARCHITECTURE.md` — module layout, tech stack, layering, public API shape.
- `docs/PORTING-MAP.md` — iOS file → Android file correspondence + status.
- `ROADMAP.md` — phased plan; keep its checkboxes updated as work lands.

## Tech stack
Kotlin · Jetpack Compose · Coroutines/Flow · Apollo Kotlin (GraphQL + `graphql-ws`) ·
OkHttp · DataStore · Coil. Min SDK 24. Library module `messenger-sdk`, sample `app`.

## Conventions
- Package root: `com.erxes.messenger`.
- Public API is the `ErxesMessenger` facade — keep the surface small and stable
  (`configure`, `setUser`, `clearUser`, `show`, Compose `MessengerLauncher`/host).
- UI state lives in `StateFlow` on ViewModels; collect with `collectAsStateWithLifecycle`.
- All network on `Dispatchers.IO`. Never block the main thread.
- URL construction: always strip a trailing `/` then append `/gateway/...` (see PROTOCOL.md).
- GraphQL responses can carry an `errors` array on HTTP 200 — check it.
- Keep Android idiomatic; don't transliterate SwiftUI literally. Match behaviour, not syntax.

## Build & run
```bash
./gradlew :messenger-sdk:assembleRelease   # build the library AAR
./gradlew :app:installDebug                 # install the sample on a device/emulator
./gradlew test                              # unit tests
./gradlew :messenger-sdk:publishToMavenLocal
```
(Gradle wrapper is added in Phase 0; until then these will not exist yet.)

## Working agreements
- When you finish a unit of work, tick the matching box in `ROADMAP.md` and update the
  status column in `docs/PORTING-MAP.md`.
- If the backend contract surprises you, fix `docs/PROTOCOL.md` first, then the code.
- Don't commit or push unless asked. If asked, branch first (don't commit to default).
- Secrets (real endpoint/integrationId) go in `local.properties` or sample BuildConfig,
  never hard-coded in the library.

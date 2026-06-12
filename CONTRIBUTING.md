# Contributing

Thanks for your interest in improving the **erxes Android Messenger SDK** — a native
Android port of the [erxes iOS SDK](https://github.com/erxes/erxes-ios-sdk). Contributions
of all kinds are welcome: bug reports, fixes, docs, and features.

By participating you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## Getting started

Requirements: JDK 17, Android SDK (compileSdk 35), an Android device/emulator for the sample.

```bash
git clone https://github.com/Munkhorgilb/android-sdk.git
cd android-sdk
./gradlew :messenger-sdk:assembleDebug   # build the library
./gradlew :messenger-sdk:test            # run unit tests
./gradlew :app:installDebug              # run the sample on a device/emulator
```

To try the SDK from another local project:

```bash
./gradlew :messenger-sdk:publishToMavenLocal
# then in the consumer: repositories { mavenLocal() }
#                       implementation("com.erxes:messenger-sdk:0.30.0")
```

## Project layout

- `messenger-sdk/` — the publishable library (`com.erxes.messenger`).
- `app/` — sample host application.
- `docs/` — `PROTOCOL.md` (the backend wire contract, the source of truth),
  `ARCHITECTURE.md`, and `PORTING-MAP.md` (iOS → Android file map).
- `ROADMAP.md` — phased plan; please keep its checkboxes in sync with your work.

Read `docs/` and `CLAUDE.md` before making changes — they explain the conventions.

## Conventions

- **Kotlin + Jetpack Compose**, Coroutines/Flow, min SDK 24. Keep the code Android-idiomatic;
  match behaviour with the iOS reference, not its syntax.
- Public API is the small, stable `ErxesMessenger` facade — discuss before expanding it.
- UI state lives in `StateFlow` on ViewModels, collected with `collectAsStateWithLifecycle`.
- All network on `Dispatchers.IO`; never block the main thread.
- The backend contract lives in `docs/PROTOCOL.md`. If the backend surprises you, fix the
  doc first, then the code.
- Keep parsers pure (no I/O) so they stay unit-testable.

## Tests

- Add/extend unit tests under `messenger-sdk/src/test` for parsing and networking logic
  (we use JUnit + MockWebServer). Run `./gradlew :messenger-sdk:test` before opening a PR.
- CI runs tests and assembles the AAR + sample on every push/PR.

## Pull requests

1. Fork and branch off `main` (don't commit to `main` directly).
2. Keep commits focused; write a clear message describing the *why*.
3. Ensure `./gradlew :messenger-sdk:test :messenger-sdk:assembleDebug :app:assembleDebug` passes.
4. Update `ROADMAP.md` / `docs/PORTING-MAP.md` status when you complete a unit of work.
5. Open the PR against `main` and describe the change and how you tested it.

## Reporting bugs

Open a GitHub issue with: SDK version, Android version/device, steps to reproduce, expected
vs actual behaviour, and relevant logs (filter by the `ErxesMessenger` log tag). For security
issues, **do not** open a public issue — see [SECURITY.md](SECURITY.md).

## License

This project is licensed under **AGPL-3.0**. By contributing, you agree that your
contributions will be licensed under the same terms. See [LICENSE](LICENSE).

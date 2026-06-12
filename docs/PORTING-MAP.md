# iOS → Android Porting Map

File-by-file correspondence between `erxes-ios-sdk/Sources/MessengerSDK` and this
Android library. Use this to track which iOS source has been ported.

## Concept mapping

| iOS (Swift/SwiftUI)            | Android (Kotlin/Compose)              |
|--------------------------------|----------------------------------------|
| `struct` / `class`             | `data class` / `class`                 |
| `@Published` + `ObservableObject` | `StateFlow` in a `ViewModel`        |
| `Combine` publishers           | `Flow`                                 |
| `async`/`await`, `Task {}`     | `suspend` fun, `viewModelScope.launch` |
| `UserDefaults`                 | DataStore Preferences                  |
| `URLSession`                   | OkHttp / Apollo engine                 |
| `Apollo` + `ApolloWebSocket`   | Apollo Kotlin (`apollo-runtime`)       |
| `SDWebImageSwiftUI`            | Coil (`coil-compose`)                  |
| `UIColor` / `UIImage`          | `Color` / `Painter`                    |
| SwiftUI `View`                 | `@Composable` fun                      |
| `@MainActor`                   | `Dispatchers.Main` / `withContext`     |

## File mapping

| iOS file                                   | Android target                                  | Status |
|--------------------------------------------|-------------------------------------------------|--------|
| `MessengerSDK.swift`                       | `ErxesMessenger.kt`                             | ☐ |
| `Config/MessengerConfig.swift`             | `config/MessengerConfig.kt`, `config/Appearance.kt` | ☐ |
| `Network/NetworkClient.swift`              | `network/ApolloProvider.kt`                     | ☐ |
| `Network/GraphQLClient.swift`              | `network/GraphQLClient.kt`                      | ☐ |
| `Network/Upload/FileUploader.swift`        | `network/FileUploader.kt`                       | ☐ |
| `Network/Operations/*.graphql`             | `src/main/graphql/**` (Apollo codegen)          | ☐ |
| `Network/TicketMutations.swift`            | `network/TicketMutations.kt`                     | ☐ |
| `Session/SessionManager.swift`             | `session/SessionStore.kt`                        | ☐ |
| `Utils/ObjectId.swift`                     | `session/ObjectId.kt`                            | ☐ |
| `Utils/DateParsing.swift`                  | `util/DateParsing.kt`                            | ☐ |
| `Utils/MessageGrouper.swift`               | `util/MessageGrouper.kt`                         | ☐ |
| `Utils/AttachmentURL.swift`                | `util/AttachmentUrl.kt`                          | ☐ |
| `Utils/Logger.swift`                       | `util/SdkLog.kt`                                 | ☐ |
| `Models/ConnectResponse.swift`            | `data/model/ConnectResponse.kt`                  | ☐ |
| `Models/Conversation.swift`               | `data/model/Conversation.kt`                     | ☐ |
| `Models/Message.swift`                    | `data/model/Message.kt`                          | ☐ |
| `Models/MessengerUser.swift`              | `data/model/MessengerUser.kt`                    | ☐ |
| `Models/Supporter.swift`                  | `data/model/Supporter.kt`                        | ☐ |
| `Models/Ticket.swift`                     | `data/model/Ticket.kt`                           | ☐ |
| `Messenger/ViewModels/AppViewModel.swift` | `ui/MessengerViewModel.kt` + `data/repository/MessengerRepository.kt` | ☐ |
| `Messenger/ChatViewModel.swift`           | `ui/conversation/ChatViewModel.kt`               | ☐ |
| `Messenger/ConversationListViewModel.swift`| `ui/conversation/ConversationListViewModel.kt`  | ☐ |
| `Messenger/MessengerLaunchButton.swift`   | `ui/launcher/MessengerLauncher.kt`               | ☐ |
| `Messenger/LauncherWindow.swift`          | `ui/launcher/LauncherOverlay.kt`                 | ☐ |
| `Messenger/MessengerContainerView.swift`  | `ui/MessengerHost.kt`                            | ☐ |
| `Messenger/Screens/*.swift`               | `ui/screens/*.kt` (Home, Messages, Help, Tickets…) | ☐ |
| `Messenger/Components/*.swift`            | `ui/components/*.kt`                              | ☐ |

> Mark a row ☑ when its Android target compiles and matches behaviour. Keep this
> table in sync with `ROADMAP.md` phase checkboxes.

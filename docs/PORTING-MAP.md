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
| `MessengerSDK.swift`                       | `ErxesMessenger.kt`                             | ☑ facade + connect + show() |
| `Config/MessengerConfig.swift`             | `config/MessengerConfig.kt`, `config/Appearance.kt` | ☑ |
| `Network/NetworkClient.swift`              | `network/GraphQLClient.kt`                      | ☑ (OkHttp; Apollo in Phase 3) |
| `Network/GraphQLClient.swift`              | `network/GraphQLClient.kt`                      | ☑ |
| `Network/Upload/FileUploader.swift`        | `network/FileUploader.kt`                       | ☑ |
| `Network/Operations/*.graphql`             | `network/MessengerOperations.kt` (raw strings)  | ◐ connect/supporters/browserInfo done |
| `Network/TicketMutations.swift`            | repo `createTicket()` + `network/TicketParser.kt` | ☑ |
| `Session/SessionManager.swift`             | `session/SessionStore.kt`                        | ☑ |
| `Utils/ObjectId.swift`                     | `session/ObjectId.kt`                            | ☑ |
| `Utils/DateParsing.swift`                  | `util/DateParsing.kt`                            | ☑ |
| `Utils/MessageGrouper.swift`               | `util/MessageGrouper.kt`                         | ☐ |
| `Utils/AttachmentURL.swift`                | `util/AttachmentUrl.kt`                          | ☑ |
| `Utils/Logger.swift`                       | `util/SdkLog.kt`                                 | ☑ |
| `Models/ConnectResponse.swift`            | `data/model/ConnectResponse.kt`                  | ☑ (ticketConfig/websiteApps deferred) |
| `Models/Conversation.swift`               | `data/model/Conversation.kt`                     | ☑ |
| `Models/Message.swift`                    | `data/model/Message.kt`                          | ☑ |
| `Models/MessengerUser.swift`              | `config/MessengerUser.kt`                        | ☑ |
| `Models/Supporter.swift`                  | `data/model/Supporter.kt`                        | ☑ |
| `Models/Ticket.swift`                     | `data/model/Ticket.kt` + `TicketConfig.kt`       | ☑ |
| `Messenger/ViewModels/AppViewModel.swift` | `ui/MessengerViewModel.kt` + `data/MessengerRepository.kt` | ◐ connect/supporters done; UI state Phase 5 |
| `Messenger/ChatViewModel.swift`           | `ui/conversation/ChatViewModel.kt`               | ☑ |
| `Messenger/ChatView.swift`                | `ui/conversation/ChatScreen.kt`                  | ☑ |
| `Messenger/MessageBubble.swift`           | `ui/components/MessageBubble.kt`                  | ☑ |
| `Messenger/MessengerContainerView.swift`  | `ui/MessengerActivity.kt`                        | ◐ chat only; host/list in 5b |
| `Components/TypingStatusView.swift`       | `ui/components/TypingIndicator.kt`               | ☑ |
| `Components/AvatarWithStatusView.swift`   | `ui/components/Avatar.kt`                         | ◐ avatar; online status in 5b |
| `Messenger/ConversationListViewModel.swift`| `ui/conversation/ConversationListViewModel.kt`  | ☑ |
| `Messenger/ConversationListView.swift`    | `ui/conversation/ConversationListScreen.kt`      | ☑ |
| `Messenger/MessengerLaunchButton.swift`   | `ui/launcher/MessengerLauncher.kt`               | ☑ |
| `Messenger/LauncherWindow.swift`          | `ui/launcher/ErxesMessengerHost.kt` (in MessengerLauncher.kt) | ☑ |
| `Messenger/MessengerContainerView.swift`  | `ui/MessengerRoot.kt` + `ui/MessengerActivity.kt`| ☑ |
| `Messenger/Screens/HomeView.swift`        | `ui/screens/HomeScreen.kt`                        | ☑ (+ social links) |
| `Messenger/Screens/GetNotifiedView.swift` | `ui/screens/IdentityFormScreen.kt`                | ☑ requireAuth/get-notified |
| `Messenger/Screens/HelpView.swift` + `HelpViewModel` | `ui/screens/HelpScreen.kt`             | ☐ Phase 6c |
| `Messenger/Screens/TicketsView.swift` + `CreateTicketView` | `ui/screens/TicketsScreen.kt` + `CreateTicketScreen.kt` | ☑ |
| `ViewModels/TicketListViewModel` + `TicketFormViewModel` | `ui/screens/TicketsViewModel.kt` | ☑ |
| `Messenger/Components/*.swift`            | `ui/components/*.kt`                              | ☐ |

> Mark a row ☑ when its Android target compiles and matches behaviour. Keep this
> table in sync with `ROADMAP.md` phase checkboxes.

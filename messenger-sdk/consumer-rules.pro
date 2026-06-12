# Consumer ProGuard/R8 rules — applied automatically to apps that depend on messenger-sdk.
#
# The SDK does not rely on runtime reflection (JSON is parsed via kotlinx.serialization's
# JsonElement API, not generated serializers), so very little needs keeping. OkHttp,
# kotlinx-coroutines and kotlinx-serialization ship their own consumer rules.

# Public API surface — keep so host apps obfuscating their build don't strip it.
-keep class com.erxes.messenger.ErxesMessenger { *; }
-keep class com.erxes.messenger.config.** { *; }

# Messenger activity is launched by name via the SDK; keep it discoverable.
-keep class com.erxes.messenger.ui.MessengerActivity { *; }

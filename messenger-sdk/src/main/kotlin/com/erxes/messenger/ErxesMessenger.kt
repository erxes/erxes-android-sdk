package com.erxes.messenger

import android.app.Activity
import android.content.Context
import com.erxes.messenger.config.MessengerConfig
import com.erxes.messenger.config.MessengerUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Public entry point for the erxes Android Messenger SDK. Mirrors `MessengerSDK` (iOS).
 *
 * Phase 0: configuration + identity state only. The connect handshake, UI, and realtime
 * transport are wired up in later phases (see ROADMAP.md).
 */
object ErxesMessenger {

    @Volatile
    private var appContext: Context? = null

    @Volatile
    internal var config: MessengerConfig? = null
        private set

    @Volatile
    internal var user: MessengerUser? = null
        private set

    private val _isReady = MutableStateFlow(false)

    /** Flips true once the connect handshake succeeds. The launcher shows only after this. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    /** Initialize the SDK. Call once, e.g. from `Application.onCreate()`. */
    fun configure(context: Context, config: MessengerConfig) {
        this.appContext = context.applicationContext
        this.config = config
        // TODO(Phase 1): SessionStore.bind(integrationId); start connect handshake.
    }

    /** Associate a signed-in user with conversations. Mirrors `setUser`. */
    fun setUser(user: MessengerUser) {
        this.user = user
        // TODO(Phase 1): re-run connect with the new identity.
    }

    /** Clear the current user, e.g. on logout. Mirrors `clearUser`. */
    fun clearUser() {
        this.user = null
        // TODO(Phase 1): SessionStore.clearCustomer().
    }

    /** Open the messenger UI. Mirrors `showMessenger(from:)`. */
    fun show(activity: Activity) {
        check(config != null) { "ErxesMessenger not configured — call configure() first" }
        // TODO(Phase 5): launch the messenger activity / overlay.
    }

    internal fun requireContext(): Context =
        appContext ?: error("ErxesMessenger not configured — call configure() first")
}

package com.erxes.messenger

import android.app.Activity
import android.content.Context
import com.erxes.messenger.config.MessengerConfig
import com.erxes.messenger.config.MessengerUser
import com.erxes.messenger.data.MessengerRepository
import com.erxes.messenger.data.model.ConnectResponse
import com.erxes.messenger.session.SessionStore
import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Public entry point for the erxes Android Messenger SDK. Mirrors `MessengerSDK` (iOS).
 *
 * Phase 1: configuration, identity, and the connect handshake. UI and realtime are
 * wired up in later phases (see ROADMAP.md).
 */
object ErxesMessenger {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var appContext: Context? = null

    @Volatile
    internal var config: MessengerConfig? = null
        private set

    @Volatile
    internal var user: MessengerUser? = null
        private set

    @Volatile
    private var session: SessionStore? = null

    private val _isReady = MutableStateFlow(false)

    /** Flips true once the connect handshake succeeds. The launcher shows only after this. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _connectResponse = MutableStateFlow<ConnectResponse?>(null)

    /** The latest successful connect payload (uiOptions, messengerData, ids), or null. */
    val connectResponse: StateFlow<ConnectResponse?> = _connectResponse.asStateFlow()

    private val _connectError = MutableStateFlow<String?>(null)

    /** Last connect failure message, or null when connected/idle. */
    val connectError: StateFlow<String?> = _connectError.asStateFlow()

    /** Initialize the SDK. Call once, e.g. from `Application.onCreate()`. Starts connect. */
    fun configure(context: Context, config: MessengerConfig) {
        this.appContext = context.applicationContext
        this.config = config
        this.session = SessionStore(context.applicationContext)
        startConnect()
    }

    /** Associate a signed-in user with conversations, then re-connect. Mirrors `setUser`. */
    fun setUser(user: MessengerUser) {
        this.user = user
        startConnect()
    }

    /** Clear the current user, e.g. on logout, then re-connect anonymously. Mirrors `clearUser`. */
    fun clearUser() {
        this.user = null
        val session = session ?: return
        scope.launch {
            runCatching { session.clearCustomer() }
            startConnect()
        }
    }

    /** Open the messenger UI. Mirrors `showMessenger(from:)`. */
    fun show(activity: Activity) {
        check(config != null) { "ErxesMessenger not configured — call configure() first" }
        // TODO(Phase 5): launch the messenger activity / overlay.
    }

    private fun startConnect() {
        val config = config ?: return
        val session = session ?: return
        scope.launch {
            try {
                _connectError.value = null
                val repository = MessengerRepository(config, session)
                val response = repository.connect(user, scope)
                _connectResponse.value = response
                _isReady.value = true
                SdkLog.d("connected: customerId=${response.customerId}")
            } catch (t: Throwable) {
                SdkLog.e("connect() failed", t)
                _connectError.value = t.message ?: "Connect failed"
            }
        }
    }

    internal fun requireContext(): Context =
        appContext ?: error("ErxesMessenger not configured — call configure() first")
}

package com.erxes.messenger

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.erxes.messenger.config.DisplayMode
import com.erxes.messenger.config.MessengerConfig
import com.erxes.messenger.config.MessengerUser
import com.erxes.messenger.data.MessengerRepository
import com.erxes.messenger.data.model.ConnectResponse
import com.erxes.messenger.session.SessionStore
import com.erxes.messenger.ui.MessengerActivity
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

    @Volatile
    private var repository: MessengerRepository? = null

    /** Shared repository, available once [configure] has run. */
    internal fun requireRepository(): MessengerRepository =
        repository ?: error("ErxesMessenger not configured — call configure() first")

    /** Persisted identity store, available once [configure] has run. */
    internal fun requireSession(): SessionStore =
        session ?: error("ErxesMessenger not configured — call configure() first")

    private val _isReady = MutableStateFlow(false)

    /** Flips true once the connect handshake succeeds. The launcher shows only after this. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _connectResponse = MutableStateFlow<ConnectResponse?>(null)

    /** The latest successful connect payload (uiOptions, messengerData, ids), or null. */
    val connectResponse: StateFlow<ConnectResponse?> = _connectResponse.asStateFlow()

    private val _connectError = MutableStateFlow<String?>(null)

    /** Last connect failure message, or null when connected/idle. */
    val connectError: StateFlow<String?> = _connectError.asStateFlow()

    private val _isIdentified = MutableStateFlow(false)

    /**
     * True once the visitor has an identity: a host-provided email/phone, a contact
     * captured via the requireAuth form on a previous launch, or a registered customer.
     * Gates the requireAuth form.
     */
    val isIdentified: StateFlow<Boolean> = _isIdentified.asStateFlow()

    /** Whether the connected integration requires a contact before starting a conversation. */
    val requireAuth: Boolean
        get() = _connectResponse.value?.messengerData?.requireAuth ?: false

    /** Chat-mode host action callback. Receives the tapped [com.erxes.messenger.config.ActionItem.id]. */
    @Volatile
    var onAction: ((String) -> Unit)? = null

    /** Initialize the SDK. Call once, e.g. from `Application.onCreate()`. Starts connect. */
    fun configure(context: Context, config: MessengerConfig) {
        this.appContext = context.applicationContext
        this.config = config
        val store = SessionStore(context.applicationContext)
        this.session = store
        this.repository = MessengerRepository(config, store)
        startConnect()
        // Chat mode is a full-screen, app-like experience: present it automatically as
        // soon as a host activity is in the foreground (no launcher tap), mirroring iOS.
        if (config.displayMode == DisplayMode.CHAT) {
            registerChatAutoPresent(context.applicationContext)
        }
    }

    @Volatile
    private var autoPresented = false

    /**
     * Auto-opens [MessengerActivity] once, the first time a host activity resumes, when
     * running in [DisplayMode.CHAT]. Mirrors iOS `autoPresentChatModeIfNeeded`.
     */
    private fun registerChatAutoPresent(appContext: Context) {
        val app = appContext as? Application ?: return
        autoPresented = false
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                // Don't stack on top of the messenger itself, and present only once.
                if (activity is MessengerActivity || autoPresented) return
                autoPresented = true
                activity.startActivity(MessengerActivity.intent(activity))
                app.unregisterActivityLifecycleCallbacks(this)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    /** Associate a signed-in user with conversations, then re-connect. Mirrors `setUser`. */
    fun setUser(user: MessengerUser) {
        this.user = user
        startConnect()
    }

    /**
     * Clear the current user on logout, then re-connect anonymously. Drops the in-memory user
     * *and* the persisted identity (cachedCustomerId, conversation, visitorId, …) so the next
     * connect starts as a fresh anonymous visitor rather than re-identifying the logged-out
     * customer and surfacing their old conversations. Mirrors `clearUser`.
     */
    fun clearUser() {
        this.user = null
        val session = session ?: return
        scope.launch {
            runCatching { session.clearIdentity() }
            startConnect()
        }
    }

    /** Open the messenger UI. Mirrors `showMessenger(from:)`. */
    fun show(activity: Activity) {
        check(config != null) { "ErxesMessenger not configured — call configure() first" }
        activity.startActivity(MessengerActivity.intent(activity))
    }

    /**
     * Captures the visitor's email/phone for the requireAuth flow, marking them identified.
     * Mirrors `AppViewModel.identify`. Throws on network/validation failure.
     */
    suspend fun identify(
        kind: MessengerRepository.ContactKind,
        value: String,
        firstName: String? = null,
        lastName: String? = null,
    ) {
        requireRepository().identify(kind, value, firstName, lastName)
        _isIdentified.value = true
    }

    /** Stores an email/phone for follow-up notifications (get-notified opt-in). */
    suspend fun saveGetNotified(kind: MessengerRepository.ContactKind, value: String) {
        requireRepository().saveGetNotified(kind, value)
    }

    private fun startConnect() {
        val repository = repository ?: return
        scope.launch {
            try {
                _connectError.value = null
                val response = repository.connect(user, scope)
                _connectResponse.value = response
                _isReady.value = true
                // Host-provided email/phone, or a contact captured on a previous launch,
                // counts as already identified — skip the requireAuth form then.
                val hostIdentified = !user?.email.isNullOrEmpty() || !user?.phone.isNullOrEmpty()
                _isIdentified.value = hostIdentified || (session?.isIdentified() ?: false)
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

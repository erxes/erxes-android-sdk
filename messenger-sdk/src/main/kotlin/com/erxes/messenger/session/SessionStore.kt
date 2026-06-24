package com.erxes.messenger.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.messengerDataStore: DataStore<Preferences> by preferencesDataStore(name = "erxes_messenger")

/**
 * Persists visitor/customer identity across launches. Mirrors `Session/SessionManager.swift`.
 *
 * Identity is bound to one integration: switching integrations clears the previous
 * customer/visitor/conversation so the new integration never inherits them.
 */
class SessionStore(private val context: Context) {

    private val store get() = context.messengerDataStore

    private object Keys {
        val customerId = stringPreferencesKey("cachedCustomerId")
        val visitorId = stringPreferencesKey("visitorId")
        val conversationId = stringPreferencesKey("conversationId")
        val integrationId = stringPreferencesKey("integrationId")
        val identified = booleanPreferencesKey("identified")
        val identity = stringPreferencesKey("identity")
    }

    /** Clears persisted identity when the integration changed since it was stored. */
    suspend fun bind(integrationId: String) {
        store.edit { prefs ->
            if (prefs[Keys.integrationId] != integrationId) {
                prefs.remove(Keys.customerId)
                prefs.remove(Keys.visitorId)
                prefs.remove(Keys.conversationId)
                prefs.remove(Keys.identified)
                prefs[Keys.integrationId] = integrationId
            }
        }
    }

    /**
     * Resets the cached customer when the host connects with a *different* email/phone.
     * The stale `cachedCustomerId` still points at the previous person, so the server would
     * re-identify them and surface their old conversations; clear it so connect resolves or
     * creates a customer for the new identity instead. A blank identity (anonymous re-open)
     * leaves the session untouched. Call after [bind] and before seeding `cachedCustomerId`.
     */
    suspend fun bind(email: String?, phone: String?) {
        val identity = listOfNotNull(email, phone)
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() } ?: return
        store.edit { prefs ->
            if (prefs[Keys.identity] != identity) {
                prefs.remove(Keys.customerId)
                prefs.remove(Keys.conversationId)
                prefs.remove(Keys.identified)
                prefs[Keys.identity] = identity
            }
        }
    }

    /** Returns the persisted visitor id, generating and storing one on first use. */
    suspend fun visitorId(): String {
        val existing = store.data.first()[Keys.visitorId]
        if (existing != null) return existing
        val generated = ObjectId.generate()
        store.edit { it[Keys.visitorId] = generated }
        return generated
    }

    suspend fun cachedCustomerId(): String? = store.data.first()[Keys.customerId]

    suspend fun setCachedCustomerId(id: String?) {
        store.edit { prefs ->
            if (id == null) prefs.remove(Keys.customerId) else prefs[Keys.customerId] = id
        }
    }

    suspend fun lastConversationId(): String? = store.data.first()[Keys.conversationId]

    suspend fun setLastConversationId(id: String?) {
        store.edit { prefs ->
            if (id == null) prefs.remove(Keys.conversationId) else prefs[Keys.conversationId] = id
        }
    }

    suspend fun isIdentified(): Boolean = store.data.first()[Keys.identified] ?: false

    suspend fun setIdentified(value: Boolean) {
        store.edit { it[Keys.identified] = value }
    }

    /** Clears customer-scoped state on logout, keeping the anonymous visitor id. */
    suspend fun clearCustomer() {
        store.edit { prefs ->
            prefs.remove(Keys.customerId)
            prefs.remove(Keys.conversationId)
            prefs.remove(Keys.identified)
        }
    }

    /**
     * Full logout reset. Unlike [clearCustomer], this also drops the bound identity and the
     * `visitorId`, so the next session starts as a brand-new anonymous visitor instead of
     * re-identifying the logged-out customer (cachedCustomerId) or inheriting their anonymous
     * thread (visitorId). The next [visitorId] access regenerates a fresh id.
     */
    suspend fun clearIdentity() {
        store.edit { prefs ->
            prefs.remove(Keys.customerId)
            prefs.remove(Keys.conversationId)
            prefs.remove(Keys.identified)
            prefs.remove(Keys.identity)
            prefs.remove(Keys.visitorId)
        }
    }
}

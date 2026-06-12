package com.erxes.messenger.config

/**
 * Optional identity for the current host user. Mirrors `MessengerUser` in the iOS SDK.
 * Any field may be null for anonymous visitors.
 */
data class MessengerUser(
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
)

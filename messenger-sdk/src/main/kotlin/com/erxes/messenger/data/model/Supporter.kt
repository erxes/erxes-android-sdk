package com.erxes.messenger.data.model

/** A support agent shown in the launcher/header. Mirrors `Models/Supporter.swift`. */
data class Supporter(
    val id: String,
    val fullName: String?,
    val avatar: String?,
    val isOnline: Boolean,
)

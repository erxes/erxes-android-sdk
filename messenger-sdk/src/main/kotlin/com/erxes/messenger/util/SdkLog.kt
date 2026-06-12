package com.erxes.messenger.util

import android.util.Log

/** Thin logging wrapper. Mirrors `Utils/Logger.swift` (iOS). Toggle with [enabled]. */
object SdkLog {
    private const val TAG = "ErxesMessenger"

    /** Set false to silence the SDK (e.g. in release builds). */
    @JvmStatic
    var enabled: Boolean = true

    fun d(message: String) {
        if (enabled) Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (enabled) Log.e(TAG, message, throwable)
    }
}

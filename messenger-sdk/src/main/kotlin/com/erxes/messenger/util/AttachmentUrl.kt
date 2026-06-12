package com.erxes.messenger.util

import java.net.URLEncoder

/**
 * Resolves an erxes file key into a full URL. Mirrors `Utils/AttachmentURL.swift` (iOS).
 *
 * Keys stored in the DB look like `office-erxes-io/avatar.png` and must be served via the
 * gateway's `read-file` endpoint; values that are already http(s) URLs pass through.
 */
object AttachmentUrl {

    /** Returns a full URL for [keyOrUrl], or null when it is null/blank. */
    fun resolve(keyOrUrl: String?, fileEndpoint: String): String? {
        if (keyOrUrl.isNullOrBlank()) return null
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) return keyOrUrl
        val base = fileEndpoint.trimEnd('/')
        val encoded = URLEncoder.encode(keyOrUrl, "UTF-8").replace("+", "%20")
        return "$base/gateway/read-file?key=$encoded"
    }
}

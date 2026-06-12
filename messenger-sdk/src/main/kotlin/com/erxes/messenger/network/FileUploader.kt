package com.erxes.messenger.network

import com.erxes.messenger.data.model.Attachment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** Result of a successful upload. The `url` is the storage key returned by the gateway. */
data class UploadedAttachment(
    val url: String,
    val name: String,
    val type: String,
    val size: Int,
) {
    /** Convert to an [Attachment] suitable for `widgetsInsertMessage`. */
    fun toAttachment(): Attachment = Attachment(url = url, type = type, name = name, size = size)
}

/** Thrown when an upload is rejected or fails. */
class UploadException(message: String) : Exception(message)

/**
 * Uploads a file to the messenger gateway's `/gateway/upload-file` endpoint.
 * Mirrors `Network/Upload/FileUploader.swift` (iOS) and the RN SDK's `uploadFile()`.
 *
 * The gateway accepts only PNG/JPEG and returns the storage **key as plain text**
 * (not JSON), which becomes the attachment `url`.
 */
class FileUploader(
    private val httpClient: OkHttpClient = sharedClient,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun upload(
        bytes: ByteArray,
        filename: String,
        mimeType: String,
        fileEndpoint: String,
    ): UploadedAttachment = withContext(io) {
        if (mimeType.lowercase() !in ALLOWED_MIME) {
            throw UploadException("Only PNG and JPG images can be uploaded.")
        }

        val media = mimeType.toMediaTypeOrNull()
            ?: throw UploadException("Invalid content type: $mimeType")
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", filename, bytes.toRequestBody(media))
            .build()

        val request = Request.Builder().url(uploadUrl(fileEndpoint)).post(body).build()

        httpClient.newCall(request).await().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw UploadException("Upload failed: ${text.take(200).ifEmpty { "HTTP ${response.code}" }}")
            }
            val key = text.trim()
            if (key.isEmpty()) throw UploadException("Server returned an empty file key.")
            UploadedAttachment(url = key, name = filename, type = mimeType, size = bytes.size)
        }
    }

    companion object {
        private val ALLOWED_MIME = setOf("image/png", "image/jpeg")

        /** Upload URL for a file endpoint base, stripping a trailing slash. */
        fun uploadUrl(fileEndpoint: String): String {
            val base = fileEndpoint.trimEnd('/')
            return "$base/gateway/upload-file?kind=main&maxHeight=0&maxWidth=0"
        }

        private val sharedClient: OkHttpClient by lazy { OkHttpClient() }
    }
}

package com.erxes.messenger

import com.erxes.messenger.network.FileUploader
import com.erxes.messenger.network.UploadException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FileUploaderTest {

    private lateinit var server: MockWebServer
    private val uploader = FileUploader()

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun fileEndpoint() = server.url("/").toString().trimEnd('/')

    @Test
    fun `builds upload url with query params`() {
        assertEquals(
            "https://x.io/gateway/upload-file?kind=main&maxHeight=0&maxWidth=0",
            FileUploader.uploadUrl("https://x.io/"),
        )
    }

    @Test
    fun `posts multipart and returns plain-text key as url`() = runBlocking {
        server.enqueue(MockResponse().setBody("uploads/abc123.png"))

        val result = uploader.upload(
            bytes = byteArrayOf(1, 2, 3, 4),
            filename = "pic.png",
            mimeType = "image/png",
            fileEndpoint = fileEndpoint(),
        )

        assertEquals("uploads/abc123.png", result.url)
        assertEquals("pic.png", result.name)
        assertEquals(4, result.size)
        assertEquals(result.url, result.toAttachment().url)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/gateway/upload-file?kind=main&maxHeight=0&maxWidth=0", recorded.path)
        assertTrue(recorded.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        assertTrue(recorded.body.readUtf8().contains("""name="file"; filename="pic.png""""))
    }

    @Test
    fun `rejects unsupported mime type without a request`() {
        val ex = assertThrows(UploadException::class.java) {
            runBlocking {
                uploader.upload(byteArrayOf(0), "a.gif", "image/gif", fileEndpoint())
            }
        }
        assertTrue(ex.message!!.contains("PNG"))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `throws on empty key`() {
        server.enqueue(MockResponse().setBody(""))
        val ex = assertThrows(UploadException::class.java) {
            runBlocking {
                uploader.upload(byteArrayOf(1), "a.jpg", "image/jpeg", fileEndpoint())
            }
        }
        assertTrue(ex.message!!.contains("empty"))
    }
}

package es.unizar.webeng.lab2

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import org.junit.jupiter.api.Assertions.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompressionIntegrationTest {

    @LocalServerPort
    var port: Int = 0

    private fun openConnection(path: String, acceptGzip: Boolean): HttpURLConnection {
        val url = URL("http://localhost:$port$path")
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            if (acceptGzip) setRequestProperty("Accept-Encoding", "gzip")
            connectTimeout = Duration.ofSeconds(5).toMillis().toInt()
            readTimeout = Duration.ofSeconds(5).toMillis().toInt()
            connect()
        }
    }

    private fun isBodyGzipped(con: HttpURLConnection): Boolean {
        val stream = try { con.inputStream } catch (e: Exception) { con.errorStream }
        stream.use {
            val a = it.read()
            val b = it.read()
            return (a == 0x1f && b == 0x8b)
        }
    }

    @Test
    fun `no Accept-Encoding, big response not compressed and has Content-Length`() {
        val con = openConnection("/test/large", acceptGzip = false)

        assertNull(con.getHeaderField("Content-Encoding"),
            "No Content-Encoding expected without Accept-Encoding header")
        assertNotNull(con.getHeaderField("Content-Length"),
            "Content-Length should be present when response is not compressed")

        assertFalse(isBodyGzipped(con), "Body should NOT be gzipped when client did not request gzip")

        con.disconnect()
    }

    @Test
    fun `Accept-Encoding gzip, big response compressed, vary present, content-length absent or chunked`() {
        val con = openConnection("/test/large", acceptGzip = true)

        val contentEncoding = con.getHeaderField("Content-Encoding")
        assertNotNull(contentEncoding, "Content-Encoding should be present for compressed response")
        assertTrue(contentEncoding.contains("gzip", ignoreCase = true),
            "Content-Encoding should indicate gzip")

        val vary = con.getHeaderField("Vary")
        assertNotNull(vary, "Vary header should be present when response varies by encoding")
        assertTrue(vary.contains("Accept-Encoding", ignoreCase = true),
            "Vary should include Accept-Encoding")

        val contentLength = con.getHeaderField("Content-Length")
        val transferEncoding = con.getHeaderField("Transfer-Encoding")
        val chunked = transferEncoding?.contains("chunked", ignoreCase = true) ?: false

        assertTrue(contentLength == null || chunked,
            "When compressed the server should either omit Content-Length or use chunked Transfer-Encoding (found Content-Length=$contentLength, Transfer-Encoding=$transferEncoding)")

        assertTrue(isBodyGzipped(con), "Body bytes should look like GZIP when compressed")

        con.disconnect()
    }

    @Test
    fun `Accept-Encoding gzip, small response not compressed`() {
        val con = openConnection("/test/small", acceptGzip = true)

        val contentEncoding = con.getHeaderField("Content-Encoding")
        assertTrue(contentEncoding == null || !contentEncoding.contains("gzip", ignoreCase = true),
            "Small response should not be compressed even when gzip requested")

        assertFalse(isBodyGzipped(con), "Small response body must not be gzipped")

        assertNotNull(con.getHeaderField("Content-Length"),
            "Small uncompressed response should generally include Content-Length")

        con.disconnect()
    }

    @Test
    fun `SSE endpoint must not be compressed`() {
        val con = openConnection("/test/sse", acceptGzip = true)

        val contentType = con.getHeaderField("Content-Type")
        assertNotNull(contentType, "SSE must return a Content-Type")
        assertTrue(contentType.startsWith("text/event-stream", ignoreCase = true),
            "SSE endpoint should return text/event-stream content-type")

        val contentEncoding = con.getHeaderField("Content-Encoding")
        assertTrue(contentEncoding == null || !contentEncoding.contains("gzip", ignoreCase = true),
            "SSE streaming endpoints must NOT be compressed")

        assertFalse(isBodyGzipped(con), "SSE body must not be gzipped")

        con.disconnect()
    }
}

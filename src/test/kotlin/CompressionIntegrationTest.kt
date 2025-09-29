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

    private fun isGzipped(con: HttpURLConnection): Boolean {
        val stream = try { con.inputStream } catch (e: Exception) { con.errorStream }
        stream.use {
            val a = it.read()
            val b = it.read()
            return (a == 0x1f && b == 0x8b)
        }
    }

    @Test
    fun `no Accept-Encoding, response no compressed`() {
        val con = openConnection("/test/large", acceptGzip = false)
        assertFalse(isGzipped(con), "Response no compressed with no Accept-Encoding")
        con.disconnect()
    }

    @Test
    fun `Accept-Encoding gzip, big response is compressed`() {
        val con = openConnection("/test/large", acceptGzip = true)
        assertTrue(isGzipped(con), "Big response is compressed using gzip")
        con.disconnect()
    }

    @Test
    fun `Accept-Encoding gzip, small response no compressed`() {
        val con = openConnection("/test/small", acceptGzip = true)
        assertFalse(isGzipped(con), "Small response no compressed")
        con.disconnect()
    }
}

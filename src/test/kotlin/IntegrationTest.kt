package es.unizar.webeng.lab2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ErrorPageIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should return custom error page`() {
        val headers = HttpHeaders()
        headers.add("Accept", "text/html")
        val entity = HttpEntity<String>(headers)

        val response = restTemplate.exchange(
            "http://localhost:$port/no-route",
            HttpMethod.GET,
            entity,
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).contains("<!DOCTYPE html>")
        assertThat(response.body).isNotNull
        assertThat(response.body).contains("Volver al inicio")
    }
}

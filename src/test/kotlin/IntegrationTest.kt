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

    private fun baseUrl(path: String) = "http://localhost:$port$path"

    @Test
    fun `cuando se solicita HTML y la ruta no existe, se renderiza templates error html`() {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.TEXT_HTML)
        val entity = HttpEntity<String>(headers)

        val response = restTemplate.exchange(
            baseUrl("/ruta"),
            HttpMethod.GET,
            entity,
            String::class.java
        )

        // Debe ser 404 Not Found
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

        // Content-Type debe ser HTML (puede llevar charset)
        assertThat(response.headers.contentType.toString()).contains("text/html")

        // El body debe contener algún texto único de tu error.html.
        // Cambia "Página de error personalizada" por el texto real de tu plantilla.
        assertThat(response.body).isNotNull
        assertThat(response.body).contains("Lo sentimos, ha ocurrido un error")
    }
}

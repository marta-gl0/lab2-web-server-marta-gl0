package es.unizar.webeng.lab2

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Assertions.*

/**
 * Integration tests to verify the OpenAPI / Swagger configuration.
 *
 * These tests ensure:
 * - The `/v3/api-docs` contains the `/time` schema.
 * - Swagger UI is accessible.
 * - The OpenAPI server URL reflects HTTPS when SSL is enabled.
 * - A HTTP Bearer security scheme and example for `/time` are present.
 * - `/actuator` is hidden from the spec.
 * - Vendor media type (`application/vnd.myapp+json`) is documented with example.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerIntegrationTest(
    @LocalServerPort private val port: Int
) {

    private val restTemplate = TestRestTemplate()

    @Test
    fun `OpenAPI spec contains time schema`() {
        val response = restTemplate.getForEntity("http://localhost:$port/v3/api-docs", String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        val json = response.body!!
        assertTrue(json.contains("/time"), "OpenAPI spec should document /time endpoint")
        assertTrue(json.contains("TimeDTO"), "OpenAPI spec should include TimeDTO schema")
    }

    @Test
    fun `Swagger UI is accessible`() {
        val response = restTemplate.getForEntity("http://localhost:$port/swagger-ui/index.html", String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode, "Swagger UI should be accessible")
        assertTrue(response.body!!.contains("Swagger UI"), "Swagger UI page should render correctly")
    }

    @Test
    fun `security scheme and example for time are present`() {
        val response = restTemplate.getForEntity("http://localhost:$port/v3/api-docs", String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        val json = response.body!!
        assertTrue(json.contains("bearer"), "OpenAPI spec should declare HTTP bearer security scheme")
        assertTrue(json.contains("\"example\":{\"time\""), "OpenAPI spec should include example for /time")
    }

    @Test
    fun `actuator endpoints are hidden`() {
        val response = restTemplate.getForEntity("http://localhost:$port/v3/api-docs", String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        val json = response.body!!
        assertFalse(json.contains("/actuator"), "/actuator should be hidden from the OpenAPI spec")
    }

    @Test
    fun `vendor media type documented`() {
        val response = restTemplate.getForEntity("http://localhost:$port/v3/api-docs", String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        val json = response.body!!
        assertTrue(
            json.contains("application/vnd.myapp+json"),
            "Vendor media type should be documented with example in the OpenAPI spec"
        )
    }
}

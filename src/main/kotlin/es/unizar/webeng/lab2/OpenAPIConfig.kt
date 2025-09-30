package es.unizar.webeng.lab2

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springdoc.core.models.GroupedOpenApi
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement

@Configuration
class OpenApiConfig(private val env: Environment) {

    /**
     * Main OpenAPI bean:
     * - adds bearerAuth security scheme
     * - registers a global security requirement
     * - builds servers list using server.ssl.enabled / server.port / server.address
     */
    @Bean
    fun customOpenAPI(): OpenAPI {
        val info = Info()
            .title("Lab2 API")
            .version("1.0")
            .description("Auto-generated OpenAPI documentation for Lab2")

        // Security scheme: HTTP bearer (JWT)
        val bearerScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("HTTP Bearer authentication (JWT)")

        val components = Components()
            .addSecuritySchemes("bearerAuth", bearerScheme)

        val openApi = OpenAPI()
            .info(info)
            .components(components)
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))

        val sslEnabled = env.getProperty("server.ssl.enabled", "false").toBoolean()
        val port = env.getProperty("server.port") ?: env.getProperty("local.server.port") ?: "8080"
        val host = env.getProperty("server.address") ?: "localhost"
        val scheme = if (sslEnabled) "https" else "http"
        val url = "$scheme://$host:$port"
        openApi.servers(listOf(Server().url(url).description("Generated server url")))

        return openApi
    }

    /**
     * Exclude actuator endpoints from the public group so /actuator doesn't appear in /v3/api-docs
     */
    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToExclude("/actuator/**")
            .build()
    }
}

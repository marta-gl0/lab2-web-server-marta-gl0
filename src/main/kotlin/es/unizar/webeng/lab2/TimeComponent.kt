package es.unizar.webeng.lab2

import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation 
import io.swagger.v3.oas.annotations.media.Content 
import io.swagger.v3.oas.annotations.media.ExampleObject 
import io.swagger.v3.oas.annotations.media.Schema 
import io.swagger.v3.oas.annotations.responses.ApiResponse

/** 
* Data Transfer Object (DTO) representing the current server time. 
*
* @property time the current local date-time on the server. 
*/
data class TimeDTO(val time: LocalDateTime)

/** 
* Contract for providing the current time. 
*/
interface TimeProvider {
    /** 
    * Returns the current local date-time. 
    * 
    * @return a [LocalDateTime] representing "now". 
    */
    fun now(): LocalDateTime 
}

/** 
* Default implementation of [TimeProvider] that returns the system time. 
*/
@Service 
class TimeService : TimeProvider {
    /** 
    * Provides the current system time using [LocalDateTime.now]. 
    */
    override fun now(): LocalDateTime = LocalDateTime.now() 
}

/** 
* Extension function to map a [LocalDateTime] to its [TimeDTO] representation. 
*/
fun LocalDateTime.toDTO(): TimeDTO = TimeDTO(time = this)

/** 
* REST controller exposing the `/time` endpoint. 
* 
* It returns the current server time as JSON or as a custom vendor-specific 
* media type (`application/vnd.myapp+json`). 
*/
@RestController
class TimeController(private val service: TimeProvider) {
    /** 
    * Returns the current server time. 
    * 
    * This endpoint is documented for Swagger/OpenAPI. It includes 
    * response examples for both `application/json` and a vendor media type. 
    */
    @Operation( 
        summary = "Get current server time", 
        description = "Returns the current local date-time of the server.", 
        responses = [ ApiResponse( 
            responseCode = "200", 
            description = "Successful response with the current time", 
            content = [ Content( 
                mediaType = "application/json", 
                schema = Schema(implementation = TimeDTO::class), 
                examples = [ ExampleObject(value = """{"time":"2025-09-30T11:32:16.4693641"}""") ] ), 
                    Content( 
                        mediaType = "application/vnd.myapp+json", 
                        schema = Schema(implementation = TimeDTO::class), 
                        examples = [ ExampleObject(value = """{"time":"2025-09-30T11:32:16.4693641"}""") ] 
                    ) 
                ] 
            ) 
        ] 
    )
    @GetMapping("/time") fun time(): TimeDTO = service.now().toDTO() 
}

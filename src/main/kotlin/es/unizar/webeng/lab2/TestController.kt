package es.unizar.webeng.lab2

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation 
import io.swagger.v3.oas.annotations.media.Content 
import io.swagger.v3.oas.annotations.media.ExampleObject 
import io.swagger.v3.oas.annotations.media.Schema 
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

/** 
* REST controller that exposes endpoints for testing 
* HTTP response compression. 
* 
* Provides both a "large" response (eligible for compression) 
* and a "small" response (below the configured threshold and 
* therefore not compressed). 
*/
@RestController
class TestController {

    /** 
    * Returns a large JSON string (~2KB) to force compression 
    * when the client requests `Accept-Encoding: gzip`. 
    * 
    * This endpoint is useful to validate that compression works 
    * for sufficiently large responses. 
    */
    @Operation( 
        summary = "Get large test payload", 
        description = "Returns a large JSON payload (~2000 characters) to test compression.", 
        responses = [ ApiResponse( 
            responseCode = "200", 
            description = "Large response eligible for compression", 
            content = [ Content( 
                mediaType = "application/json", 
                schema = Schema(type = "string"), 
                examples = [ ExampleObject( value = "\"xxxxxxxxxx... (2000 characters) ...\"" ) ] 
            ) ] 
        ) ] 
    )
    @GetMapping("/test/large", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun large(): String = "x".repeat(2000)

    /** 
    * Returns a small JSON string (< 1KB) which should not be compressed, 
    * even if the client requests `Accept-Encoding: gzip`. 
    * 
    * This endpoint is useful to validate that compression is not applied 
    * below the configured minimum response size. 
    */
    @Operation( 
        summary = "Get small test payload", 
        description = "Returns a small JSON payload to confirm it is not compressed.", 
        responses = [ ApiResponse( 
            responseCode = "200", 
            description = "Small response not compressed", 
            content = [ Content( 
                mediaType = "application/json", 
                schema = Schema(type = "string"), 
                examples = [ ExampleObject( value = """{"msg":"small"}""" ) ] 
            ) ] 
        ) ] 
    )
    @GetMapping("/test/small", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun small(): String = """{"msg":"small"}"""

    /**
     * SSE (Server-Sent Events) endpoint that streams a simple message.
     *
     * This endpoint produces responses with the media type
     * `text/event-stream` and must **not** be compressed, even when
     * the client requests `Accept-Encoding: gzip`.
     *
     * It sends a single `"hello"` event and then completes.
     * SSE endpoints are useful for testing that streaming responses
     * are excluded from HTTP response compression.
     */
     @Operation(
        summary = "Stream SSE event",
        description = "Streams a single `hello` event using Server-Sent Events (SSE). " +
                      "This endpoint demonstrates that `text/event-stream` responses " +
                      "are not compressed, even if the client requests gzip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "SSE stream started successfully",
                content = [
                    Content(
                        mediaType = "text/event-stream",
                        schema = Schema(type = "string"),
                        examples = [
                            ExampleObject(value = "data: hello\n\n")
                        ]
                    )
                ]
            )
        ]
    )
    @GetMapping("/test/sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sse(): SseEmitter {
        val emitter = SseEmitter(5_000L)
        Thread {
            try {
                emitter.send("data: hello\n\n")
                emitter.complete()
            } catch (ex: IOException) {
                emitter.completeWithError(ex)
            }
        }.start()
        return emitter
    }
}

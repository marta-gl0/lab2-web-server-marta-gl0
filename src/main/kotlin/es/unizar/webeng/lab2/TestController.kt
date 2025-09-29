package es.unizar.webeng.lab2

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test/large", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun large(): String = "x".repeat(2000)

    @GetMapping("/test/small", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun small(): String = """{"msg":"small"}"""
}

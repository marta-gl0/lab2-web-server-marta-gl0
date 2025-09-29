package es.unizar.webeng.lab2

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class MVCTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var service: TimeProvider

    @Test
    fun `GET time should return fixed LocalDateTime`() {
        val fixedTime = LocalDateTime.of(2025, 9, 25, 12, 34, 56)
        given(service.now()).willReturn(fixedTime)

        mockMvc.perform(get("/time"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.time").value(containsString("2025-09-25T12:34:56")))
    }
}


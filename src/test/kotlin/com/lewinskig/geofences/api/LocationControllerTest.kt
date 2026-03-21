package com.lewinskig.geofences.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class LocationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should accept valid location update`() {
        mockMvc.post("/location") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "trackId": "track-1",
                    "lat": 52.2297,
                    "lng": 21.0122,
                    "timestamp": "2026-03-21T10:00:00Z"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }
    }
}

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

    fun postLocation(location: String) =
        mockMvc.post("/location") {
            contentType = MediaType.APPLICATION_JSON
            content = location
        }

    @Test
    fun `should accept valid location update`() {
        postLocation(
            """
                {
                    "trackId": "track-1",
                    "lat": 52.2297,
                    "lng": 21.0122,
                    "timestamp": "2026-03-21T10:00:00Z"
                }
            """
        ).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should return 400 when trackId is blank`() {
        postLocation(
            """
                {
                    "trackId": "   ",
                    "lat": 52.2297,
                    "lng": 21.0122,
                    "timestamp": "2026-03-21T10:00:00Z"
                }
            """
        ).andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 when lat is out of range`() {
        postLocation(
            """
                {
                    "trackId": "track-1",
                    "lat": 91.0,
                    "lng": 21.0122,
                    "timestamp": "2026-03-21T10:00:00Z"
                }
            """
        ).andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 when lng is out of range`() {
        postLocation(
            """
                {
                    "trackId": "track-1",
                    "lat": 52.2297,
                    "lng": 181.0,
                    "timestamp": "2026-03-21T10:00:00Z"
                }
            """
        ).andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 400 when timestamp is missing`() {
        postLocation(
            """
                {
                    "trackId": "track-1",
                    "lat": 52.2297,
                    "lng": 21.0122
                }
            """
        ).andExpect {
            status { isBadRequest() }
        }
    }
}

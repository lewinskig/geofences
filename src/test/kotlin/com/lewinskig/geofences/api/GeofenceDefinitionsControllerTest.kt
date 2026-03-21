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
class GeofenceDefinitionsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    fun createGeofence(geofence: String) =
        mockMvc.post("/geofences") {
            contentType = MediaType.APPLICATION_JSON
            content = geofence
        }

    @Test
    fun `should create geofence and return its id`() {
        createGeofence(
            """
                {
                    "name": "Rynak Główny Kraków",
                    "polygon": [
                        { "lng":19.935821473251963, "lat": 50.06139701200041},
                        { "lng":19.93768906692037, "lat": 50.06060881854114},
                        { "lng":19.93884766669592, "lat": 50.062007575497404},
                        { "lng":19.93696278049387, "lat": 50.062684736832495},
                        { "lng":19.935821473251963, "lat": 50.06139701200041}
                    ]
                }
            """
        ).andExpect {
            status { isOk() }
            jsonPath("$.geofenceId") { isNotEmpty() }
        }
    }
}


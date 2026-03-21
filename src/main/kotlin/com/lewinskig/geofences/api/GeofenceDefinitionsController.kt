package com.lewinskig.geofences.api

import com.lewinskig.geofences.application.GeofenceId
import com.lewinskig.geofences.application.LatLng
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GeofenceDefinitionsController {
    private val logger = LoggerFactory.getLogger(GeofenceDefinitionsController::class.java)

    @PostMapping("/geofences")
    fun createGeofence(@RequestBody geofenceDefinitionRequest: GeofenceDefinitionRequest): GeofenceDefinitionResponse {
        logger.info("Received geofence definition: {}", geofenceDefinitionRequest)
        return GeofenceDefinitionResponse(geofenceId = GeofenceId.randomGeofenceId())
    }
}

data class GeofenceDefinitionRequest(val name: String, val polygon: List<LatLng>)
data class GeofenceDefinitionResponse(val geofenceId: GeofenceId)
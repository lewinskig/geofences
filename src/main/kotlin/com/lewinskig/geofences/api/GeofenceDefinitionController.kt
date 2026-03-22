package com.lewinskig.geofences.api

import com.lewinskig.geofences.application.GeofenceService
import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.geofence.GeofenceFactory
import com.lewinskig.geofences.application.geofence.GeofenceId
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GeofenceDefinitionController(
    val geofenceService: GeofenceService,
    val geofenceFactory: GeofenceFactory,
) {
    private val logger = LoggerFactory.getLogger(GeofenceDefinitionController::class.java)

    @PostMapping("/geofences")
    fun createGeofence(@RequestBody request: CreateGeofenceRequest): CreateGeofenceResponse {
        logger.info("Received geofence definition: {}", request)

        val geofence = geofenceFactory.createGeofence(request.name, request.polygon)
        geofenceService.create(geofence)
        return CreateGeofenceResponse(geofence.geofenceId)
    }

    @GetMapping("/geofences")
    fun getAllGeofences(): GetAllGeofencesResponse {
        logger.info("Fetching all geofences")
        return geofenceService.getAll()
            .map(::GeofenceDefinitionDto)
            .let(::GetAllGeofencesResponse)
    }

    @DeleteMapping("/geofences/{id}")
    fun deleteGeofence(@PathVariable id: String): ResponseEntity<Unit> {
        logger.info("Deleting geofence: {}", id)
        return when (geofenceService.delete(GeofenceId(id))) {
            true -> ResponseEntity.ok()
            false -> ResponseEntity.notFound()
        }.build()
    }
}

data class CreateGeofenceRequest(val name: String, val polygon: List<LatLng>)
data class CreateGeofenceResponse(val geofenceId: GeofenceId)

data class GetAllGeofencesResponse(val geofences: List<GeofenceDefinitionDto>)
data class GeofenceDefinitionDto(val geofenceId: GeofenceId, val name: String, val polygon: List<LatLng>) {
    constructor(geofence: Geofence) : this(
        geofenceId = geofence.geofenceId,
        name = geofence.name,
        polygon = geofence.geometry.toPolygon()
    )
}

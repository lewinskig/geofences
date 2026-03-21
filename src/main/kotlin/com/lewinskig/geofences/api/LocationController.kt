package com.lewinskig.geofences.api

import com.lewinskig.geofences.storage.LocationUpdateRepository
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class LocationController(
    val locationUpdateRepository: LocationUpdateRepository
) {

    private val logger = LoggerFactory.getLogger(LocationController::class.java)

    @PostMapping("/location")
    fun updateLocation(@RequestBody location: LocationRequest) {
        logger.info("Received location update: trackId={}, lat={}, lng={}, timestamp={}", location.trackId, location.lat, location.lng, location.timestamp)
        locationUpdateRepository.insert(location)
    }
}

data class LocationRequest(
    val trackId: String,
    val lat: Double,
    val lng: Double,
    val timestamp: Instant,
) {
    init {
        require(trackId.isNotBlank()) { "trackId must not be blank" }
        require(lat in -90.0..90.0) { "lat must be between -90 and 90" }
        require(lng in -180.0..180.0) { "lng must be between -180 and 180" }
    }
}
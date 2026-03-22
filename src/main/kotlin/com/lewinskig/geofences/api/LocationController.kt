package com.lewinskig.geofences.api

import com.lewinskig.geofences.application.GeofenceService
import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.tracker.Tracker
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class LocationController(
    val geofenceService: GeofenceService
) {
    private val logger = LoggerFactory.getLogger(LocationController::class.java)

    @PostMapping("/location")
    fun updateLocation(@RequestBody location: LocationRequest) {
        logger.info(
            "Received location update: trackId={}, lat={}, lng={}, timestamp={}",
            location.trackId,
            location.lat,
            location.lng,
            location.timestamp
        )
        val tracker = Tracker(
            trackId = location.trackId,
            latlng = LatLng(location.lat, location.lng),
            timestamp = location.timestamp
        )
        geofenceService.evaluateLocation(tracker)
    }
}

data class LocationRequest(val trackId: String, val lat: Double, val lng: Double, val timestamp: Instant)
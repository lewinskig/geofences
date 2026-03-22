package com.lewinskig.geofences.application.tracker

import com.lewinskig.geofences.application.LatLng
import java.time.Instant

data class Tracker(
    val trackId: String,
    val latlng: LatLng,
    val timestamp: Instant,
) {
    init {
        require(trackId.isNotBlank()) { "trackId must not be blank" }
    }
}
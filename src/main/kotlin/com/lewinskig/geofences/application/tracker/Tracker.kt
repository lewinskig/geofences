package com.lewinskig.geofences.application.tracker

import com.lewinskig.geofences.application.LatLng
import java.time.Instant

data class Tracker(
    val trackerId: TrackerId,
    val latlng: LatLng,
    val timestamp: Instant,
)
package com.lewinskig.geofences.application.geofence

import com.lewinskig.geofences.application.geometry.GeofenceGeometry
import java.time.Instant

data class Geofence(
    val geofenceId: GeofenceId,
    val name: String,
    val geometry: GeofenceGeometry,
    val createdAt: Instant,
)
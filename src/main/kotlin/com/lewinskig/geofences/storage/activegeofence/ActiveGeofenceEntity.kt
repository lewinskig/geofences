package com.lewinskig.geofences.storage.activegeofence

import com.lewinskig.geofences.application.geofence.GeofenceId
import java.time.Instant

data class ActiveGeofenceEntity(
    val trackId: String,
    val geofenceId: GeofenceId,
    val enteredAt: Instant
)
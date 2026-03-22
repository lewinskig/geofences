package com.lewinskig.geofences.storage.activegeofence

import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.tracker.TrackerId
import java.time.Instant

data class ActiveGeofenceEntity(
    val trackId: TrackerId,
    val geofenceId: GeofenceId,
    val enteredAt: Instant
)
package com.lewinskig.geofences.application.notification

import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.tracker.TrackerId
import java.time.Instant

interface NotificationService {
    fun publish(event: GeofenceTransition)
}

data class GeofenceTransition(
    val geofenceId: GeofenceId,
    val trackerId: TrackerId,
    val type: TransitionType,
    val timestamp: Instant
)

enum class TransitionType {ENTERED, EXITED}
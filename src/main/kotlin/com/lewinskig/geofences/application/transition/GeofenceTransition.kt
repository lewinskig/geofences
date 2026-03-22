package com.lewinskig.geofences.application.transition

import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.tracker.TrackerId
import java.time.Instant

data class GeofenceTransition(
    val geofenceId: GeofenceId,
    val trackerId: TrackerId,
    val type: TransitionType,
    val timestamp: Instant
) {
    companion object {
        fun entered(geofenceId: GeofenceId, trackerId: TrackerId, timestamp: Instant) = GeofenceTransition(
            geofenceId = geofenceId,
            trackerId = trackerId,
            TransitionType.ENTERED,
            timestamp
        )

        fun exited(geofenceId: GeofenceId, trackerId: TrackerId, timestamp: Instant) = GeofenceTransition(
            geofenceId = geofenceId,
            trackerId = trackerId,
            TransitionType.EXITED,
            timestamp
        )
    }
}
package com.lewinskig.geofences.application.geofence

import com.lewinskig.geofences.application.geometry.GeofenceGeometry
import com.lewinskig.geofences.application.tracker.Tracker
import java.time.Instant

data class Geofence(
    val geofenceId: GeofenceId,
    val name: String,
    val geometry: GeofenceGeometry,
    val createdAt: Instant,
) {
    fun containsTracker(tracker: Tracker): Boolean {
        return geometry.containsCoordinate(tracker.latlng.toCoordinate())
    }
}
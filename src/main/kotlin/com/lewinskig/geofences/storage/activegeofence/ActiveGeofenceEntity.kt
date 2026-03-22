package com.lewinskig.geofences.storage.activegeofence

import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.tracker.TrackerId
import java.sql.ResultSet
import java.time.Instant

data class ActiveGeofenceEntity(
    val trackId: TrackerId,
    val geofenceId: GeofenceId,
    val enteredAt: Instant
) {
    constructor(rs: ResultSet): this(
        trackId = TrackerId(rs.getString("track_id")),
        geofenceId = GeofenceId(rs.getString("geofence_id")),
        enteredAt = rs.getTimestamp("entered_at").toInstant()
    )
}
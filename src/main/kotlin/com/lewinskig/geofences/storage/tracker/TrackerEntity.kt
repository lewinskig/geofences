package com.lewinskig.geofences.storage.tracker

import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.application.tracker.TrackerId
import java.sql.ResultSet
import java.time.Instant

data class TrackerEntity(
    val trackerId: TrackerId,
    val lastSeenLatitude: Double?,
    val lastSeenLongitude: Double?,
    val lastRecordedAt: Instant?,
    val lastReceivedAt: Instant?,
) {
    constructor(rs: ResultSet) : this(
        trackerId = TrackerId(rs.getString("id")),
        lastSeenLatitude = rs.getObject("last_seen_latitude") as Double?,
        lastSeenLongitude = rs.getObject("last_seen_longitude") as Double?,
        lastRecordedAt = rs.getTimestamp("last_recorded_at")?.toInstant(),
        lastReceivedAt = rs.getTimestamp("last_received_at")?.toInstant()
    )
    constructor(tracker: Tracker, receivedAt: Instant) : this(
        trackerId = tracker.trackerId,
        lastSeenLatitude = tracker.latlng.lat,
        lastSeenLongitude = tracker.latlng.lng,
        lastRecordedAt = tracker.timestamp,
        lastReceivedAt = receivedAt
    )
}
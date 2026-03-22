package com.lewinskig.geofences.storage.locationupdate

import com.lewinskig.geofences.application.tracker.Tracker
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class LocationUpdateRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun insert(tracker: Tracker) {
        jdbcTemplate.update(
            """
            insert into location_updates(track_id, latitude, longitude, recorded_at)
            values (?, ?, ?, ?)
            """.trimIndent(),
            tracker.trackerId.id,
            tracker.latlng.lat,
            tracker.latlng.lng,
            OffsetDateTime.ofInstant(tracker.timestamp, ZoneOffset.UTC),
        )
    }
}
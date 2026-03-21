package com.lewinskig.geofences.storage

import com.lewinskig.geofences.api.LocationRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class LocationUpdateRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun insert(location: LocationRequest) {
        jdbcTemplate.update(
            """
            insert into location_updates(track_id, latitude, longitude, recorded_at)
            values (?, ?, ?, ?)
            """.trimIndent(),
            location.trackId,
            location.lat,
            location.lng,
            OffsetDateTime.ofInstant(location.timestamp, ZoneOffset.UTC),
        )
    }
}
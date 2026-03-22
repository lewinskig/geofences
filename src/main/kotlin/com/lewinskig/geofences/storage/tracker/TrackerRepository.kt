package com.lewinskig.geofences.storage.tracker

import com.lewinskig.geofences.application.tracker.TrackerId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class TrackerRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun ensureExists(trackerId: TrackerId) {
        jdbcTemplate.update(
            """
            insert into tracker(id)
            values (?)
            on conflict (id) do nothing
            """,
            trackerId.id
        )
    }

    fun findByIdForUpdate(trackerId: TrackerId): TrackerEntity {
        return jdbcTemplate.queryForObject(
            """
            select id,
                   last_seen_latitude,
                   last_seen_longitude,
                   last_recorded_at,
                   last_received_at
            from tracker
            where id = ?
            for update
            """,
            {rs, _ -> TrackerEntity(rs)},
            trackerId.id
        ) ?: error("Tracker ${trackerId.id} not found")
    }

    fun updateLastSeen(entity: TrackerEntity) {
        jdbcTemplate.update(
            """
            update tracker
            set last_seen_latitude = ?,
                last_seen_longitude = ?,
                last_recorded_at = ?,
                last_received_at = ?
            where id = ?
            """.trimIndent(),
            entity.lastSeenLatitude,
            entity.lastSeenLongitude,
            OffsetDateTime.ofInstant(entity.lastRecordedAt, ZoneOffset.UTC),
            OffsetDateTime.ofInstant(entity.lastReceivedAt, ZoneOffset.UTC),
            entity.trackerId.id
        )
    }
}
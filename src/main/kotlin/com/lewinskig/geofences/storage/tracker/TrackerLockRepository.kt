package com.lewinskig.geofences.storage.tracker

import com.lewinskig.geofences.application.tracker.TrackerId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TrackerLockRepository(
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

    fun lock(trackerId: TrackerId) {
        jdbcTemplate.queryForObject(
            """
            select id
            from tracker
            where id = ?
            for update
            """,
            String::class.java,
            trackerId.id
        )
    }
}
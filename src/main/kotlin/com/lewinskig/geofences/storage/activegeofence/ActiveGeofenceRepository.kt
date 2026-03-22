package com.lewinskig.geofences.storage.activegeofence

import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.application.tracker.TrackerId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime.ofInstant
import java.time.ZoneOffset.UTC

@Repository
class ActiveGeofenceRepository(
    private val jdbcTemplate: JdbcTemplate,
) {

    fun geofenceEntered(entity: ActiveGeofenceEntity) {
        jdbcTemplate.update(
            """
            insert into 
                active_geofence(track_id, geofence_id, entered_at)
            values 
                (?, ?, ?)
            """,
            entity.trackId.id,
            entity.geofenceId.uuid,
            ofInstant(entity.enteredAt, UTC)
        )
    }

    fun geofenceExited(trackId: TrackerId, geofenceId: GeofenceId) {
        jdbcTemplate.update(
            """
            delete from 
                active_geofence
            where
                track_id = ? and geofence_id = ?
            """,
            trackId.id,
            geofenceId.uuid
        )
    }

    fun findActiveGeofences(tracker: Tracker): List<ActiveGeofenceEntity> =
        jdbcTemplate.query(
            """
            select
                track_id, 
                geofence_id, 
                entered_at
            from 
                active_geofence
            where
             track_id = ?
            """,
            { rs, _ ->
                ActiveGeofenceEntity(
                    trackId = TrackerId(rs.getString("track_id")),
                    geofenceId = GeofenceId(rs.getString("geofence_id")),
                    enteredAt = rs.getTimestamp("entered_at").toInstant()
                )
            },
            tracker.trackerId.id
        )
}


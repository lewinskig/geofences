package com.lewinskig.geofences.storage.geofencedefinition

import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.geofence.GeofenceId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime.ofInstant
import java.time.ZoneOffset.UTC

@Repository
class GeofenceDefinitionRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun insert(entity: GeofenceDefinitionEntity) {
        jdbcTemplate.update(
            """
            insert into geofence_definition( 
                id,
                name,
                geometryWkt,
                envelope_min_lat,
                envelope_min_lng, 
                envelope_max_lat,
                envelope_max_lng,
                created_at) 
              values (?, ?, ?, ?, ? , ?, ?, ?)
            """.trimIndent(),
            entity.id.uuid,
            entity.name,
            entity.geometryWkt,
            entity.envelope.minLat,
            entity.envelope.minLng,
            entity.envelope.maxLat,
            entity.envelope.maxLng,
            ofInstant(entity.createdAt, UTC)
        )
    }

    fun findByPointInEnvelope(point: LatLng): List<GeofenceDefinitionEntity> =
        jdbcTemplate.query(
            """
        select 
            id, 
            name, 
            geometryWkt, 
            envelope_min_lat, 
            envelope_min_lng, 
            envelope_max_lat,
            envelope_max_lng, 
            created_at 
        from 
            geofence_definition 
        where 
            envelope_min_lat <= ? 
            and envelope_max_lat >= ? 
            and envelope_min_lng <= ? 
            and envelope_max_lng >= ?
        """,
            { rs, _ -> GeofenceDefinitionEntity(rs) },
            point.lat,
            point.lat,
            point.lng,
            point.lng
        )

    fun findAll(): List<GeofenceDefinitionEntity> =
        jdbcTemplate.query(
            """
        select 
            id, 
            name, 
            geometryWkt, 
            envelope_min_lat, 
            envelope_min_lng, 
            envelope_max_lat,
            envelope_max_lng, 
            created_at 
        from 
            geofence_definition
        """,
            { rs, _ -> GeofenceDefinitionEntity(rs) }
        )

    fun deleteById(geofenceId: GeofenceId): Boolean {
        val rowsAffected = jdbcTemplate.update(
            "DELETE FROM geofence_definition WHERE id = ?",
            geofenceId.uuid
        )
        return rowsAffected > 0
    }
}


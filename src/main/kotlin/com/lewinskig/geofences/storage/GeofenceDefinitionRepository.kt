package com.lewinskig.geofences.storage

import com.lewinskig.geofences.application.GeofenceId
import com.lewinskig.geofences.application.geometry.GeofenceEnvelope
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class GeofenceDefinitionRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun insert(entity: GeofenceDefinitionEntity) {
        with(entity) {
            jdbcTemplate.update(
                """
            insert into geofence_definition( id, name, geometryWkt, envelope_min_lat, envelope_min_lng, envelope_max_lat,
             envelope_max_lng, created_at ) values (?, ?, ?, ?, ? ,?,?,?)
            """.trimIndent(),
                entity.id.uuid,
                entity.name,
                entity.geometryWkt,
                entity.envelope.minLat,
                entity.envelope.minLng,
                entity.envelope.maxLat,
                entity.envelope.maxLng,
                OffsetDateTime.ofInstant(entity.createdAt, ZoneOffset.UTC)
            )
        }
    }
}

data class GeofenceDefinitionEntity(
    val id: GeofenceId,
    val name: String,
    val geometryWkt: String,
    val envelope: GeofenceEnvelope,
    val createdAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Geofence name must not be blank" }
        require(name.length < 255) { "Geofence name must be less than 255 characters" }
    }
}
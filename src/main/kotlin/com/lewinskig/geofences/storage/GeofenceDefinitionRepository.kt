package com.lewinskig.geofences.storage

import com.lewinskig.geofences.application.GeofenceId
import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.geometry.GeofenceEnvelope
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

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
            OffsetDateTime.ofInstant(entity.createdAt, ZoneOffset.UTC)
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

    constructor(rs: ResultSet) : this(
        id = GeofenceId(rs.getString("id")),
        name = rs.getString("name"),
        geometryWkt = rs.getString("geometryWkt"),
        envelope = GeofenceEnvelope(
            minLat = rs.getDouble("envelope_min_lat"),
            minLng = rs.getDouble("envelope_min_lng"),
            maxLat = rs.getDouble("envelope_max_lat"),
            maxLng = rs.getDouble("envelope_max_lng")
        ),
        createdAt = rs.getTimestamp("created_at").toInstant()
    )
}
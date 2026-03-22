package com.lewinskig.geofences.storage

import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.geometry.GeofenceEnvelope
import java.sql.ResultSet
import java.time.Instant

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
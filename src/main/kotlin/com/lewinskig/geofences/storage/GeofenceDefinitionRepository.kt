package com.lewinskig.geofences.storage

import com.lewinskig.geofences.application.GeofenceId
import com.lewinskig.geofences.application.geometry.GeofenceEnvelope
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class GeofenceDefinitionRepository() {
    fun insert(geofenceDefinitionEntity: GeofenceDefinitionEntity) {
        TODO("Not yet implemented")
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
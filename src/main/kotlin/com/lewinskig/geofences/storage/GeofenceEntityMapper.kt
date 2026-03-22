package com.lewinskig.geofences.storage

import com.lewinskig.geofences.application.geofence.Geofence
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GeofenceEntityMapper @Autowired constructor(
    val geofenceGeometryWtkMapper: GeofenceGeometryWtkMapper,
) {

    fun toEntity(geofence: Geofence) = GeofenceDefinitionEntity(
        id = geofence.geofenceId,
        name = geofence.name,
        geometryWkt = geofenceGeometryWtkMapper.toWkt(geofence.geometry),
        envelope = geofence.geometry.envelope(),
        createdAt = geofence.createdAt,
    )

    fun fromEntity(entity: GeofenceDefinitionEntity) = Geofence(
        geofenceId = entity.id,
        name = entity.name,
        geometry = geofenceGeometryWtkMapper.fromWkt(entity.geometryWkt),
        createdAt = entity.createdAt,
    )
}
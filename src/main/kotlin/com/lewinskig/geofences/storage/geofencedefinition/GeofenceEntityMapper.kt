package com.lewinskig.geofences.storage.geofencedefinition

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceGeometryWtkMapper
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

    fun toDomain(entity: GeofenceDefinitionEntity) = Geofence(
        geofenceId = entity.id,
        name = entity.name,
        geometry = geofenceGeometryWtkMapper.fromWkt(entity.geometryWkt),
        createdAt = entity.createdAt,
    )
}
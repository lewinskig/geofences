package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geometry.GeofenceGeometryFactory
import com.lewinskig.geofences.storage.GeofenceDefinitionEntity
import com.lewinskig.geofences.storage.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.GeofenceGeometryWtkMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class GeofenceService @Autowired constructor(
    val repository: GeofenceDefinitionRepository,
    val geofenceGeometryFactory: GeofenceGeometryFactory,
    val geofenceGeometryWtkMapper: GeofenceGeometryWtkMapper,
    val clock: Clock
) {
    fun insert(name: String, polygon: List<LatLng>): GeofenceId {
        val geofenceId = GeofenceId.randomGeofenceId()
        val geofenceGeometry = geofenceGeometryFactory.createGeofenceGeometry(polygon)
        val geometryWkt = geofenceGeometryWtkMapper.toWkt(geofenceGeometry)
        val envelope = geofenceGeometry.envelope()

        repository.insert(
            GeofenceDefinitionEntity(
                id = geofenceId,
                name = name,
                geometryWkt = geometryWkt,
                envelope = envelope,
                createdAt = clock.instant()
            )
        )
        return geofenceId
    }
}
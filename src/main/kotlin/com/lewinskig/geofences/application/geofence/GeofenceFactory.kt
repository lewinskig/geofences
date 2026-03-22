package com.lewinskig.geofences.application.geofence

import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.geometry.GeofenceGeometryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class GeofenceFactory @Autowired constructor(
    val geofenceGeometryFactory: GeofenceGeometryFactory,
    val clock: Clock
) {
    fun createGeofence(name: String, polygon: List<LatLng>): Geofence =
        Geofence(
            geofenceId = GeofenceId.randomGeofenceId(),
            name = name,
            geometry = geofenceGeometryFactory.createGeofenceGeometry(polygon),
            createdAt = clock.instant(),
        )
}


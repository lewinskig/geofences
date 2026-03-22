package com.lewinskig.geofences.application.geometry

import com.lewinskig.geofences.application.LatLng
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GeofenceGeometryFactory @Autowired constructor(val geometryFactory: GeometryFactory) {

    fun createGeofenceGeometry(polygon: List<LatLng>): GeofenceGeometry {
        val coordinates = closeRingIfNeeded(polygon).map(LatLng::toCoordinate).toTypedArray()
        val linearRing = geometryFactory.createLinearRing(coordinates)
        val geometry = geometryFactory.createPolygon(linearRing, null)

        if (geometry.isValid.not()) {
            throw IllegalArgumentException("Invalid polygon geometry")
        }
        return GeofenceGeometry(geometry)
    }

    private fun closeRingIfNeeded(polygon: List<LatLng>): List<LatLng> {
        if (polygon.isEmpty()) return polygon
        val first = polygon.first()
        val last = polygon.last()

        return if (first == last) polygon
        else polygon + first
    }
}
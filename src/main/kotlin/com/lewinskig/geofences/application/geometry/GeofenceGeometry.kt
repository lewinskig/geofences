package com.lewinskig.geofences.application.geometry

import com.lewinskig.geofences.application.LatLng
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry

class GeofenceGeometry(val geometry: Geometry) {
    fun envelope(): GeofenceEnvelope = geometry.envelopeInternal.run {
        GeofenceEnvelope(minLat = minY, maxLat = maxY, minLng = minX, maxLng = maxX)
    }

    fun containsCoordinate(coordinate: Coordinate): Boolean =
        geometry.contains(geometry.factory.createPoint(coordinate))

    fun toPolygon(): List<LatLng> =
        geometry.coordinates.map(::LatLng)
}


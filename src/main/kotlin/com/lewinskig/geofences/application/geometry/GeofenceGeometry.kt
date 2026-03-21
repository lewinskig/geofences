package com.lewinskig.geofences.application.geometry

import org.locationtech.jts.geom.Geometry

class GeofenceGeometry(val geometry: Geometry) {
    fun envelope(): GeofenceEnvelope = geometry.envelopeInternal.run {
        GeofenceEnvelope(minLat = minY, maxLat = maxY, minLng = minX, maxLng = maxX)
    }
}


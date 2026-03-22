package com.lewinskig.geofences.storage.geofencedefinition

import com.lewinskig.geofences.application.geometry.GeofenceGeometry
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GeofenceGeometryWtkMapper @Autowired constructor(
    val wktReader: WKTReader,
    val wktWriter: WKTWriter
) {
    fun toWkt(geofence: GeofenceGeometry): String =
        wktWriter.write(geofence.geometry)

    fun fromWkt(geometry: String): GeofenceGeometry =
        GeofenceGeometry(wktReader.read(geometry))
}
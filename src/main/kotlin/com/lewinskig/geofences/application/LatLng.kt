package com.lewinskig.geofences.application

import org.locationtech.jts.geom.Coordinate

data class LatLng(val lat: Double, val lng: Double) {
    init {
        require(lat in -90.0..90.0) { "lat must be between -90 and 90" }
        require(lng in -180.0..180.0) { "lng must be between -180 and 180" }
    }

    constructor(coordinate: Coordinate) : this(coordinate.y, coordinate.x)

    fun toCoordinate() = Coordinate(lng, lat)
}
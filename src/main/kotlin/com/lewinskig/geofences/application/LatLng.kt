package com.lewinskig.geofences.application

data class LatLng(val lat: Double, val lng: Double) {
    init {
        require(lat in -90.0..90.0) { "lat must be between -90 and 90" }
        require(lng in -180.0..180.0) { "lng must be between -180 and 180" }
    }
}
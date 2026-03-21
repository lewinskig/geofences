package com.lewinskig.geofences.application.geometry

data class GeofenceEnvelope(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
)
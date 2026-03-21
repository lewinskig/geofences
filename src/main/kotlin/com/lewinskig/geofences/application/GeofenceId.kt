package com.lewinskig.geofences.application

import java.util.UUID.randomUUID

data class GeofenceId(val geofenceId: String) {
    init {
        require(geofenceId.isNotBlank()) { "geofenceId must not be blank" }
    }

    override fun toString(): String = geofenceId

    companion object {
        fun randomGeofenceId() = GeofenceId(randomUUID().toString())
    }
}
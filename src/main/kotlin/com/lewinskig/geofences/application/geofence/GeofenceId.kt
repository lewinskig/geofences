package com.lewinskig.geofences.application.geofence

import java.util.UUID

data class GeofenceId(val uuid: UUID) {
    constructor(uuid: String) : this(UUID.fromString(uuid))

    override fun toString(): String = uuid.toString()

    companion object {
        fun randomGeofenceId() = GeofenceId(UUID.randomUUID())
    }
}
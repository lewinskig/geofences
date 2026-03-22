package com.lewinskig.geofences.application

import java.util.UUID
import java.util.UUID.randomUUID

data class GeofenceId(val uuid: UUID) {
    constructor(uuid: String) : this(UUID.fromString(uuid))

    override fun toString(): String = uuid.toString()

    companion object {
        fun randomGeofenceId() = GeofenceId(randomUUID())
    }
}
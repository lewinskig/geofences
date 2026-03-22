package com.lewinskig.geofences.application.notification

import com.lewinskig.geofences.application.transition.GeofenceTransition

interface NotificationService {
    fun publish(event: GeofenceTransition)
}
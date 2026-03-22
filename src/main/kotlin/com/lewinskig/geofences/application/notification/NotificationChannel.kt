package com.lewinskig.geofences.application.notification

import com.lewinskig.geofences.application.transition.Transition

interface NotificationChannel {
    fun publish(event: Transition)
}
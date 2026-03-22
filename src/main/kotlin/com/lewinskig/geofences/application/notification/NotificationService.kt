package com.lewinskig.geofences.application.notification

import com.lewinskig.geofences.application.transition.Transition

interface NotificationService {
    fun publish(event: Transition)
}
package com.lewinskig.geofences.application.notification

import com.lewinskig.geofences.application.transition.Transition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotificationService @Autowired constructor(
    val notificationChannels: List<NotificationChannel>
){
    fun publish(event: Transition) =
        notificationChannels.forEach { it.publish(event) }
}
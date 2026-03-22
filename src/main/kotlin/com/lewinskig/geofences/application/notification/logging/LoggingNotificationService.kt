package com.lewinskig.geofences.application.notification.logging

import com.lewinskig.geofences.application.transition.Transition
import com.lewinskig.geofences.application.notification.NotificationService
import org.slf4j.LoggerFactory

class LoggingNotificationService : NotificationService {
    private val logger = LoggerFactory.getLogger(LoggingNotificationService::class.java)

    override fun publish(event: Transition) =
        logger.info("Publishing geofence transition: {}", event)
}
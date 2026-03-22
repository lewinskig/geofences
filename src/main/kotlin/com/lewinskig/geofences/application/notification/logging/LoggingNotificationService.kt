package com.lewinskig.geofences.application.notification.logging

import com.lewinskig.geofences.application.notification.GeofenceTransition
import com.lewinskig.geofences.application.notification.NotificationService
import org.slf4j.LoggerFactory

class LoggingNotificationService : NotificationService {
    private val logger = LoggerFactory.getLogger(LoggingNotificationService::class.java)

    override fun publish(event: GeofenceTransition) =
        logger.info("Publishing geofence transition: {}", event)
}
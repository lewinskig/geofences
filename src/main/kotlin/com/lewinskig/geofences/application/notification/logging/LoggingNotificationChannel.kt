package com.lewinskig.geofences.application.notification.logging

import com.lewinskig.geofences.application.notification.NotificationChannel
import com.lewinskig.geofences.application.transition.Transition
import org.slf4j.LoggerFactory

class LoggingNotificationChannel : NotificationChannel {
    private val logger = LoggerFactory.getLogger(LoggingNotificationChannel::class.java)

    override fun publish(event: Transition) =
        logger.info("Publishing geofence transition: {}", event)
}
package com.lewinskig.geofences.application.notification

import com.lewinskig.geofences.application.notification.logging.LoggingNotificationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NotificationConfiguration {

    @Bean
    fun notificationService(): NotificationService = LoggingNotificationService()
}
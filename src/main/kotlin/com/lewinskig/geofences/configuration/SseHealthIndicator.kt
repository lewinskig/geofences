package com.lewinskig.geofences.configuration

import com.lewinskig.geofences.application.notification.sse.SseNotificationChannel
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component

@Component
class SseHealthIndicator(
    private val sseNotificationChannel: SseNotificationChannel
) : HealthIndicator {

    override fun health(): Health {
        val activeClients = sseNotificationChannel.getActiveClientsCount()
        return Health.up()
            .withDetail("activeClients", activeClients)
            .build()
    }
}


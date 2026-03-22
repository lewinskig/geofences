package com.lewinskig.geofences.api

import com.lewinskig.geofences.application.notification.sse.SseNotificationChannel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class NotificationController @Autowired constructor(
    val sseNotificationChannel: SseNotificationChannel
) {
    @GetMapping("/notifications")
    fun streamNotifications(): SseEmitter {
        return sseNotificationChannel.registerEmitter()
    }
}
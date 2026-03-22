package com.lewinskig.geofences.application.notification.sse

import com.lewinskig.geofences.application.notification.NotificationChannel
import com.lewinskig.geofences.application.transition.Transition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.CopyOnWriteArrayList

@Service
class SseNotificationChannel @Autowired constructor(
    val objectMapper: ObjectMapper
) : NotificationChannel {
    private val logger = LoggerFactory.getLogger(SseNotificationChannel::class.java)
    private val eventEmitters: CopyOnWriteArrayList<SseEmitter> = CopyOnWriteArrayList()

    @Bean
    fun closeEmittersOnGracefulShutdown(): ApplicationListener<ContextClosedEvent> =
        ApplicationListener {
            logger.info("Closing SSE notification service")
            eventEmitters.forEach(SseEmitter::complete)
            eventEmitters.clear()
        }

    override fun publish(event: Transition) {
        val eventAsString = objectMapper.writeValueAsString(event)
        eventEmitters.forEach { eventEmitter ->
            try {
                eventEmitter.send(eventAsString)
            } catch (e: Exception) {
                val removed = eventEmitters.remove(eventEmitter)
                logger.warn(
                    "Failed to send event. Emitter removed from queue {}",
                    if (removed) "successfully" else "unsuccessfully",
                    e
                )
            }
        }
    }

    fun registerEmitter(): SseEmitter {
        val emitter = SseEmitter(-1L)
        emitter.onCompletion {
            logger.info("notification emitter {} completed", emitter)
            eventEmitters.remove(emitter)
        }
        emitter.onTimeout {
            logger.warn("notification emitter {} timedout", emitter)
            eventEmitters.remove(emitter)
        }
        emitter.onError {
            logger.warn("notification emitter {} error", emitter)
            eventEmitters.remove(emitter)
        }
        eventEmitters.add(emitter)
        logger.info("notification emitter registered, the pool is now {}", eventEmitters.size)
        return emitter
    }
}
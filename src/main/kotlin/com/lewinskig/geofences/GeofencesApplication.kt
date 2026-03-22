package com.lewinskig.geofences

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@SpringBootApplication
class GeofencesApplication {
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}

fun main(args: Array<String>) {
    runApplication<GeofencesApplication>(*args)
}

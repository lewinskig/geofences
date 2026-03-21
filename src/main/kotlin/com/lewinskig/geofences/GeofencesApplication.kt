package com.lewinskig.geofences

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

@SpringBootApplication
class GeofencesApplication {
    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()

    @Bean
    fun geometryFactory() = GeometryFactory()

    @Bean
    fun wktReader() = WKTReader()

    @Bean
    fun wktWriter() = WKTWriter()
}

fun main(args: Array<String>) {
    runApplication<GeofencesApplication>(*args)
}

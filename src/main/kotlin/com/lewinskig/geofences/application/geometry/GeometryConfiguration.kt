package com.lewinskig.geofences.application.geometry

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeometryConfiguration {
    @Bean
    fun geometryFactory() = GeometryFactory()

    @Bean
    fun wktReader() = WKTReader()

    @Bean
    fun wktWriter() = WKTWriter()
}
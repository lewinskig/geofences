package com.lewinskig.geofences.application.geometry

import com.lewinskig.geofences.application.LatLng
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GeofenceGeometryFactoryTest @Autowired constructor(
    private val geofenceGeometryFactory: GeofenceGeometryFactory
) {
    @Test
    fun `should create valid geometry from closed polygon`() {
        // given
        val closedPolygon = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0),
            LatLng(0.0, 0.0)
        )

        // when
        val geometry = geofenceGeometryFactory.createGeofenceGeometry(closedPolygon)

        // then
        assertNotNull(geometry)
        assertEquals(closedPolygon, geometry.toPolygon())
    }

    @Test
    fun `should automatically close ring when first point differs from last`() {
        // given
        val openPolygon = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )

        // when
        val geometry = geofenceGeometryFactory.createGeofenceGeometry(openPolygon)

        // then
        val expectedClosedPolygon = openPolygon + openPolygon.first()
        assertEquals(expectedClosedPolygon, geometry.toPolygon())
    }

    @Test
    fun `should create triangle geometry`() {
        // given
        val triangle = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 0.0)
        )

        // when
        val geometry = geofenceGeometryFactory.createGeofenceGeometry(triangle)

        // then
        val expectedClosedTriangle = triangle + triangle.first()
        assertEquals(expectedClosedTriangle, geometry.toPolygon())
    }

    @Test
    fun `should throw exception for self-intersecting polygon`() {
        // given
        val selfIntersectingPolygon = listOf(
            LatLng(0.0, 0.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0),
            LatLng(0.0, 1.0)
        )

        // when & then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            geofenceGeometryFactory.createGeofenceGeometry(selfIntersectingPolygon)
        }
        assertEquals("Invalid polygon geometry", exception.message)
    }
}
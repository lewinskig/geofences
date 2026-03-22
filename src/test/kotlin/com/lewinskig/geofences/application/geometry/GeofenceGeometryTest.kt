package com.lewinskig.geofences.application.geometry

import com.lewinskig.geofences.application.LatLng
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GeofenceGeometryTest @Autowired constructor(
    private val geofenceGeometryFactory: GeofenceGeometryFactory
) {
    @Nested
    inner class Envelope {

        @Test
        fun `should return bounding box of the geometry`() {
            // given
            val polygon = listOf(
                LatLng(lat = 0.0, lng = 0.0),
                LatLng(lat = 0.0, lng = 3.0),
                LatLng(lat = 2.0, lng = 3.0),
                LatLng(lat = 2.0, lng = 0.0)
            )
            val geometry = geofenceGeometryFactory.createGeofenceGeometry(polygon)

            // when
            val envelope = geometry.envelope()

            // then
            assertEquals(
                GeofenceEnvelope(minLat = 0.0, maxLat = 2.0, minLng = 0.0, maxLng = 3.0),
                envelope
            )
        }

        @Test
        fun `should return envelope for triangle`() {
            // given
            val triangle = listOf(
                LatLng(lat = 0.0, lng = 0.0),
                LatLng(lat = 2.0, lng = 1.0),
                LatLng(lat = 0.0, lng = 2.0)
            )
            val geometry = geofenceGeometryFactory.createGeofenceGeometry(triangle)

            // when
            val envelope = geometry.envelope()

            // then
            assertEquals(
                GeofenceEnvelope(minLat = 0.0, maxLat = 2.0, minLng = 0.0, maxLng = 2.0),
                envelope
            )
        }
    }

    @Nested
    inner class ContainsCoordinate {

        @Test
        fun `should return true when coordinate is inside polygon`() {
            // given
            val square = listOf(
                LatLng(lat = 0.0, lng = 0.0),
                LatLng(lat = 0.0, lng = 10.0),
                LatLng(lat = 10.0, lng = 10.0),
                LatLng(lat = 10.0, lng = 0.0)
            )
            val geometry = geofenceGeometryFactory.createGeofenceGeometry(square)
            val pointInside = Coordinate(5.0, 5.0)

            // when
            val result = geometry.containsCoordinate(pointInside)

            // then
            assertTrue(result)
        }

        @Test
        fun `should return false when coordinate is outside polygon`() {
            // given
            val square = listOf(
                LatLng(lat = 0.0, lng = 0.0),
                LatLng(lat = 0.0, lng = 10.0),
                LatLng(lat = 10.0, lng = 10.0),
                LatLng(lat = 10.0, lng = 0.0)
            )
            val geometry = geofenceGeometryFactory.createGeofenceGeometry(square)
            val pointOutside = Coordinate(15.0, 15.0)

            // when
            val result = geometry.containsCoordinate(pointOutside)

            // then
            assertFalse(result)
        }

        @Test
        fun `should return false when coordinate is on the edge`() {
            // given
            val square = listOf(
                LatLng(lat = 0.0, lng = 0.0),
                LatLng(lat = 0.0, lng = 10.0),
                LatLng(lat = 10.0, lng = 10.0),
                LatLng(lat = 10.0, lng = 0.0)
            )
            val geometry = geofenceGeometryFactory.createGeofenceGeometry(square)
            val pointOnBottomEdge = Coordinate(5.0, 0.0)

            // when
            val result = geometry.containsCoordinate(pointOnBottomEdge)

            // then
            assertFalse(result)
        }
    }

    @Nested
    inner class ToPolygon {

        @Test
        fun `should return list of LatLng points representing polygon vertices`() {
            // given
            val originalPolygon = listOf(
                LatLng(lat = 1.0, lng = 2.0),
                LatLng(lat = 1.0, lng = 5.0),
                LatLng(lat = 4.0, lng = 5.0),
                LatLng(lat = 4.0, lng = 2.0)
            )
            val geometry = geofenceGeometryFactory.createGeofenceGeometry(originalPolygon)

            // when
            val result = geometry.toPolygon()

            // then
            val expectedClosedPolygon = originalPolygon + originalPolygon.first()
            assertEquals(expectedClosedPolygon, result)
        }

        @Test
        fun `should preserve coordinate precision`() {
            // given
            val precisePolygon = listOf(
                LatLng(lat = 52.2297, lng = 21.0122),  // Warszawa
                LatLng(lat = 52.2298, lng = 21.0125),
                LatLng(lat = 52.2295, lng = 21.0127)
            )
            val geometry = geofenceGeometryFactory.createGeofenceGeometry(precisePolygon)

            // when
            val result = geometry.toPolygon()

            // then
            assertEquals(precisePolygon.first(), result.first())
            assertEquals(52.2297, result.first().lat)
            assertEquals(21.0122, result.first().lng)
        }
    }
}
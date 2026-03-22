package com.lewinskig.geofences.storage.geofencedefinition

import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.geometry.GeofenceEnvelope
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionEntity
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class GeofenceDefinitionRepositoryTest {

    @Autowired
    private lateinit var repository: GeofenceDefinitionRepository

    @Test
    fun `should insert geofence definition`() {
        // given
        val entity = createGeofenceEntity(
            name = "Test Geofence",
            envelope = GeofenceEnvelope(
                minLat = 50.0,
                maxLat = 51.0,
                minLng = 19.0,
                maxLng = 20.0
            )
        )

        // when
        repository.insert(entity)

        // then - no exception means success
    }

    @Test
    fun `should find geofence when point is inside envelope`() {
        // given
        val entity = createGeofenceEntity(
            name = "Warsaw Area",
            envelope = GeofenceEnvelope(
                minLat = 52.0,
                maxLat = 52.5,
                minLng = 20.5,
                maxLng = 21.5
            )
        )
        repository.insert(entity)

        val pointInside = LatLng(lat = 52.25, lng = 21.0)

        // when
        val result = repository.findByPointInEnvelope(pointInside)

        // then
        assertEquals(1, result.size)
        assertEquals(entity.id, result[0].id)
        assertEquals("Warsaw Area", result[0].name)
    }

    @Test
    fun `should not find geofence when point is outside envelope`() {
        // given
        val entity = createGeofenceEntity(
            name = "Krakow Area",
            envelope = GeofenceEnvelope(
                minLat = 50.0,
                maxLat = 50.1,
                minLng = 19.9,
                maxLng = 20.0
            )
        )
        repository.insert(entity)

        val pointOutside = LatLng(lat = 52.0, lng = 21.0) // Warsaw - far from Krakow

        // when
        val result = repository.findByPointInEnvelope(pointOutside)

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should find two geofences when envelopes overlap`() {
        // given
        val geofence1 = createGeofenceEntity(
            name = "Zone A",
            envelope = GeofenceEnvelope(
                minLat = 50.0,
                maxLat = 51.0,
                minLng = 19.0,
                maxLng = 20.0
            )
        )
        val geofence2 = createGeofenceEntity(
            name = "Zone B",
            envelope = GeofenceEnvelope(
                minLat = 50.5,
                maxLat = 51.5,
                minLng = 19.5,
                maxLng = 20.5
            )
        )
        repository.insert(geofence1)
        repository.insert(geofence2)

        val pointInOverlap = LatLng(lat = 50.75, lng = 19.75) // inside both envelopes

        // when
        val result = repository.findByPointInEnvelope(pointInOverlap)

        // then
        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "Zone A" })
        assertTrue(result.any { it.name == "Zone B" })
    }

    private fun createGeofenceEntity(name: String, envelope: GeofenceEnvelope) =
        GeofenceDefinitionEntity(
            id = GeofenceId.randomGeofenceId(),
            name = name,
            geometryWkt = "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))",
            envelope = envelope,
            createdAt = Instant.now()
        )
}
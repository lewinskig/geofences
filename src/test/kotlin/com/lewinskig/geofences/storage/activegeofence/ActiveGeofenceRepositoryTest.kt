package com.lewinskig.geofences.storage.activegeofence

import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.geometry.GeofenceEnvelope
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.application.tracker.TrackerId
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionEntity
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class ActiveGeofenceRepositoryTest {

    @Autowired
    private lateinit var repository: ActiveGeofenceRepository

    @Autowired
    private lateinit var geofenceDefinitionRepository: GeofenceDefinitionRepository

    private lateinit var geofenceId: GeofenceId
    private val trackerId = TrackerId("tracker-123")
    private val enteredAt = Instant.parse("2026-03-22T10:00:00Z")

    @BeforeEach
    fun setUp() {
        // Tworzymy geofence definition bo active_geofence ma foreign key
        geofenceId = GeofenceId.randomGeofenceId()
        geofenceDefinitionRepository.insert(createGeofenceDefinition(geofenceId))
    }

    @Nested
    inner class GeofenceEntered {

        @Test
        fun `should insert active geofence entry`() {
            // given
            val entity = ActiveGeofenceEntity(trackerId, geofenceId, enteredAt)

            // when
            repository.geofenceEntered(entity)

            // then
            val tracker = createTracker(trackerId)
            val result = repository.findActiveGeofences(tracker)
            assertEquals(1, result.size)
            assertEquals(trackerId, result[0].trackId)
            assertEquals(geofenceId, result[0].geofenceId)
            assertEquals(enteredAt, result[0].enteredAt)
        }

        @Test
        fun `should allow same tracker to be in multiple geofences`() {
            // given
            val geofenceId2 = GeofenceId.randomGeofenceId()
            geofenceDefinitionRepository.insert(createGeofenceDefinition(geofenceId2))

            repository.geofenceEntered(ActiveGeofenceEntity(trackerId, geofenceId, enteredAt))
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId, geofenceId2, enteredAt))

            // when
            val tracker = createTracker(trackerId)
            val result = repository.findActiveGeofences(tracker)

            // then
            assertEquals(2, result.size)
        }
    }

    @Nested
    inner class GeofenceExited {

        @Test
        fun `should remove active geofence entry`() {
            // given
            val entity = ActiveGeofenceEntity(trackerId, geofenceId, enteredAt)
            repository.geofenceEntered(entity)

            // when
            repository.geofenceExited(entity)

            // then
            val tracker = createTracker(trackerId)
            val result = repository.findActiveGeofences(tracker)
            assertTrue(result.isEmpty())
        }

        @Test
        fun `should only remove specific tracker-geofence pair`() {
            // given
            val trackerId2 = TrackerId("tracker-456")
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId, geofenceId, enteredAt))
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId2, geofenceId, enteredAt))

            // when - usuwamy tylko trackerId
            repository.geofenceExited(ActiveGeofenceEntity(trackerId, geofenceId, enteredAt))

            // then - trackerId2 nadal powinien być aktywny
            val result = repository.findByGeofenceId(geofenceId)
            assertEquals(1, result.size)
            assertEquals(trackerId2, result[0].trackId)
        }
    }

    @Nested
    inner class FindActiveGeofences {

        @Test
        fun `should return empty list when tracker has no active geofences`() {
            // given
            val tracker = createTracker(trackerId)

            // when
            val result = repository.findActiveGeofences(tracker)

            // then
            assertTrue(result.isEmpty())
        }

        @Test
        fun `should return only geofences for specific tracker`() {
            // given
            val trackerId2 = TrackerId("tracker-456")
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId, geofenceId, enteredAt))
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId2, geofenceId, enteredAt))

            // when
            val tracker = createTracker(trackerId)
            val result = repository.findActiveGeofences(tracker)

            // then
            assertEquals(1, result.size)
            assertEquals(trackerId, result[0].trackId)
        }
    }

    @Nested
    inner class FindByGeofenceId {

        @Test
        fun `should return all trackers in specific geofence`() {
            // given
            val trackerId2 = TrackerId("tracker-456")
            val trackerId3 = TrackerId("tracker-789")
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId, geofenceId, enteredAt))
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId2, geofenceId, enteredAt))
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId3, geofenceId, enteredAt))

            // when
            val result = repository.findByGeofenceId(geofenceId)

            // then
            assertEquals(3, result.size)
            assertTrue(result.map { it.trackId }.containsAll(listOf(trackerId, trackerId2, trackerId3)))
        }

        @Test
        fun `should return empty list when no trackers in geofence`() {
            // when
            val result = repository.findByGeofenceId(geofenceId)

            // then
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class DeleteByGeofenceId {

        @Test
        fun `should delete all entries for specific geofence`() {
            // given
            val trackerId2 = TrackerId("tracker-456")
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId, geofenceId, enteredAt))
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId2, geofenceId, enteredAt))

            // when
            val deletedCount = repository.deleteByGeofenceId(geofenceId)

            // then
            assertEquals(2, deletedCount)
            assertTrue(repository.findByGeofenceId(geofenceId).isEmpty())
        }

        @Test
        fun `should return 0 when no entries to delete`() {
            // when
            val deletedCount = repository.deleteByGeofenceId(geofenceId)

            // then
            assertEquals(0, deletedCount)
        }

        @Test
        fun `should not affect entries in other geofences`() {
            // given
            val geofenceId2 = GeofenceId.randomGeofenceId()
            geofenceDefinitionRepository.insert(createGeofenceDefinition(geofenceId2))

            repository.geofenceEntered(ActiveGeofenceEntity(trackerId, geofenceId, enteredAt))
            repository.geofenceEntered(ActiveGeofenceEntity(trackerId, geofenceId2, enteredAt))

            // when
            repository.deleteByGeofenceId(geofenceId)

            // then - wpis w geofenceId2 nadal istnieje
            val remaining = repository.findByGeofenceId(geofenceId2)
            assertEquals(1, remaining.size)
            assertEquals(trackerId, remaining[0].trackId)
        }
    }

    private fun createTracker(trackerId: TrackerId) = Tracker(
        trackerId = trackerId,
        latlng = LatLng(52.0, 21.0),
        timestamp = Instant.now()
    )

    private fun createGeofenceDefinition(id: GeofenceId) = GeofenceDefinitionEntity(
        id = id,
        name = "Test Geofence",
        geometryWkt = "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))",
        envelope = GeofenceEnvelope(minLat = 0.0, maxLat = 1.0, minLng = 0.0, maxLng = 1.0),
        createdAt = Instant.now()
    )
}
package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.geometry.GeofenceGeometry
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceEntity
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionEntity
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceEntityMapper
import com.lewinskig.geofences.storage.locationupdate.LocationUpdateRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class GeofenceServiceTest {

    private val geofenceDefinitionRepository = mockk<GeofenceDefinitionRepository>(relaxed = true)
    private val geofenceEntityMapper = mockk<GeofenceEntityMapper>()
    private val locationUpdateRepository = mockk<LocationUpdateRepository>(relaxed = true)
    private val activeGeofenceRepository = mockk<ActiveGeofenceRepository>(relaxed = true)

    private val fixedInstant = Instant.parse("2026-03-22T10:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    private lateinit var service: GeofenceService

    @BeforeEach
    fun setUp() {
        service = GeofenceService(
            geofenceDefinitionRepository,
            geofenceEntityMapper,
            locationUpdateRepository,
            activeGeofenceRepository,
            clock
        )
    }

    @Nested
    inner class CreateNew {

        @Test
        fun `should map geofence to entity and insert into repository`() {
            // given
            val geofence = mockk<Geofence>()
            val entity = mockk<GeofenceDefinitionEntity>()
            every { geofenceEntityMapper.toEntity(geofence) } returns entity

            // when
            service.createNew(geofence)

            // then
            verify { geofenceEntityMapper.toEntity(geofence) }
            verify { geofenceDefinitionRepository.insert(entity) }
        }
    }

    @Nested
    inner class EvaluateLocation {

        private val trackId = "tracker-123"
        private val geofenceId = GeofenceId.randomGeofenceId()

        @Test
        fun `should always save location update for history`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns emptyList()

            // when
            service.evaluateLocation(tracker)

            // then
            verify { locationUpdateRepository.insert(tracker) }
        }

        @Test
        fun `should record entry when tracker enters geofence for the first time`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            val geofenceEntity = mockk<GeofenceDefinitionEntity>()
            val geofence = createGeofenceThatContainsTracker(true)

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns listOf(geofenceEntity)
            every { geofenceEntityMapper.toDomain(geofenceEntity) } returns geofence

            // when
            service.evaluateLocation(tracker)

            // then
            verify {
                activeGeofenceRepository.geofenceEntered(
                    ActiveGeofenceEntity(
                        trackId = trackId,
                        geofenceId = geofenceId,
                        enteredAt = fixedInstant
                    )
                )
            }
        }

        @Test
        fun `should record exit when tracker leaves active geofence`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            val geofenceEntity = mockk<GeofenceDefinitionEntity>()
            val geofence = createGeofenceThatContainsTracker(false)
            val activeGeofence = ActiveGeofenceEntity(
                trackId = trackId,
                geofenceId = geofenceId,
                enteredAt = Instant.parse("2026-03-22T09:00:00Z")
            )

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns listOf(activeGeofence)
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns listOf(geofenceEntity)
            every { geofenceEntityMapper.toDomain(geofenceEntity) } returns geofence

            // when
            service.evaluateLocation(tracker)

            // then
            verify { activeGeofenceRepository.geofenceExited(trackId, geofenceId) }
        }

        @Test
        fun `should not record anything when tracker stays inside active geofence`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            val geofenceEntity = mockk<GeofenceDefinitionEntity>()
            val geofence = createGeofenceThatContainsTracker(true)
            val activeGeofence = ActiveGeofenceEntity(
                trackId = trackId,
                geofenceId = geofenceId,
                enteredAt = Instant.parse("2026-03-22T09:00:00Z")
            )

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns listOf(activeGeofence)
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns listOf(geofenceEntity)
            every { geofenceEntityMapper.toDomain(geofenceEntity) } returns geofence

            // when
            service.evaluateLocation(tracker)

            // then
            verify(exactly = 0) { activeGeofenceRepository.geofenceEntered(any()) }
            verify(exactly = 0) { activeGeofenceRepository.geofenceExited(any(), any()) }
        }

        @Test
        fun `should not record entry when envelope matches but tracker is outside actual geometry (false positive)`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            val geofenceEntity = mockk<GeofenceDefinitionEntity>()
            val geofence = createGeofenceThatContainsTracker(false)

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns listOf(geofenceEntity)
            every { geofenceEntityMapper.toDomain(geofenceEntity) } returns geofence

            // when
            service.evaluateLocation(tracker)

            // then
            verify(exactly = 0) { activeGeofenceRepository.geofenceEntered(any()) }
        }

        @Test
        fun `should handle multiple geofences independently`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            
            val geofenceId1 = GeofenceId.randomGeofenceId()
            val geofenceId2 = GeofenceId.randomGeofenceId()
            
            val geofenceEntity1 = mockk<GeofenceDefinitionEntity>()
            val geofenceEntity2 = mockk<GeofenceDefinitionEntity>()
            
            val geofence1 = createGeofenceWithId(geofenceId1, containsTracker = true)
            val geofence2 = createGeofenceWithId(geofenceId2, containsTracker = true)

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns listOf(geofenceEntity1, geofenceEntity2)
            every { geofenceEntityMapper.toDomain(geofenceEntity1) } returns geofence1
            every { geofenceEntityMapper.toDomain(geofenceEntity2) } returns geofence2

            // when
            service.evaluateLocation(tracker)

            // then
            verify { activeGeofenceRepository.geofenceEntered(match { it.geofenceId == geofenceId1 }) }
            verify { activeGeofenceRepository.geofenceEntered(match { it.geofenceId == geofenceId2 }) }
        }

        private fun createTracker(lat: Double, lng: Double) = Tracker(
            trackId = trackId,
            latlng = LatLng(lat, lng),
            timestamp = fixedInstant
        )

        private fun createGeofenceThatContainsTracker(contains: Boolean): Geofence {
            val geometry = mockk<GeofenceGeometry>()
            every { geometry.containsCoordinate(any()) } returns contains
            
            return Geofence(
                geofenceId = geofenceId,
                name = "Test Geofence",
                geometry = geometry,
                createdAt = fixedInstant
            )
        }

        private fun createGeofenceWithId(id: GeofenceId, containsTracker: Boolean): Geofence {
            val geometry = mockk<GeofenceGeometry>()
            every { geometry.containsCoordinate(any()) } returns containsTracker
            
            return Geofence(
                geofenceId = id,
                name = "Test Geofence ${id.uuid}",
                geometry = geometry,
                createdAt = fixedInstant
            )
        }
    }
}
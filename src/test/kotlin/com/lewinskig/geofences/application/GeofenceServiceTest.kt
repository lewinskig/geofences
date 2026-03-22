package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.geometry.GeofenceGeometry
import com.lewinskig.geofences.application.notification.NotificationService
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.application.tracker.TrackerId
import com.lewinskig.geofences.application.transition.Transition
import com.lewinskig.geofences.application.transition.TransitionEvaluatorService
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceEntity
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionEntity
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceEntityMapper
import com.lewinskig.geofences.storage.locationupdate.LocationUpdateRepository
import com.lewinskig.geofences.storage.tracker.TrackerRepository
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
    private val notificationService = mockk<NotificationService>(relaxed = true)
    private val transitionEvaluatorService = mockk<TransitionEvaluatorService>()
    private val trackerRepository = mockk<TrackerRepository>(relaxed = true)

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
            notificationService,
            transitionEvaluatorService,
            trackerRepository,
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
            service.create(geofence)

            // then
            verify { geofenceEntityMapper.toEntity(geofence) }
            verify { geofenceDefinitionRepository.insert(entity) }
        }
    }

    @Nested
    inner class GetAll {

        @Test
        fun `should return all geofences mapped to domain`() {
            // given
            val entity1 = mockk<GeofenceDefinitionEntity>()
            val entity2 = mockk<GeofenceDefinitionEntity>()
            val geofence1 = mockk<Geofence>()
            val geofence2 = mockk<Geofence>()

            every { geofenceDefinitionRepository.findAll() } returns listOf(entity1, entity2)
            every { geofenceEntityMapper.toDomain(entity1) } returns geofence1
            every { geofenceEntityMapper.toDomain(entity2) } returns geofence2

            // when
            val result = service.getAll()

            // then
            assert(result == listOf(geofence1, geofence2))
        }

        @Test
        fun `should return empty list when no geofences exist`() {
            // given
            every { geofenceDefinitionRepository.findAll() } returns emptyList()

            // when
            val result = service.getAll()

            // then
            assert(result.isEmpty())
        }
    }

    @Nested
    inner class Delete {

        private val geofenceId = GeofenceId.randomGeofenceId()

        @Test
        fun `should delete geofence by id and return true when deleted`() {
            // given
            every { activeGeofenceRepository.findByGeofenceId(geofenceId) } returns emptyList()
            every { geofenceDefinitionRepository.deleteById(geofenceId) } returns true

            // when
            val result = service.delete(geofenceId)

            // then
            assert(result)
            verify { geofenceDefinitionRepository.deleteById(geofenceId) }
        }

        @Test
        fun `should return false when geofence does not exist`() {
            // given
            every { activeGeofenceRepository.findByGeofenceId(geofenceId) } returns emptyList()
            every { geofenceDefinitionRepository.deleteById(geofenceId) } returns false

            // when
            val result = service.delete(geofenceId)

            // then
            assert(!result)
        }

        @Test
        fun `should publish exit notifications for all active trackers before deleting`() {
            // given
            val tracker1 = TrackerId("tracker-1")
            val tracker2 = TrackerId("tracker-2")
            val enteredAt = fixedInstant.minusSeconds(3600)

            val activeGeofences = listOf(
                ActiveGeofenceEntity(tracker1, geofenceId, enteredAt),
                ActiveGeofenceEntity(tracker2, geofenceId, enteredAt)
            )

            every { activeGeofenceRepository.findByGeofenceId(geofenceId) } returns activeGeofences
            every { geofenceDefinitionRepository.deleteById(geofenceId) } returns true

            // when
            service.delete(geofenceId)

            // then - notyfikacje o opuszczeniu dla każdego trackera
            verify {
                notificationService.publish(
                    Transition.exited(geofenceId, tracker1, fixedInstant)
                )
            }
            verify {
                notificationService.publish(
                    Transition.exited(geofenceId, tracker2, fixedInstant)
                )
            }
        }

        @Test
        fun `should delete active geofences before deleting geofence definition`() {
            // given
            val activeGeofence = ActiveGeofenceEntity(
                TrackerId("tracker-1"),
                geofenceId,
                fixedInstant.minusSeconds(3600)
            )

            every { activeGeofenceRepository.findByGeofenceId(geofenceId) } returns listOf(activeGeofence)
            every { geofenceDefinitionRepository.deleteById(geofenceId) } returns true

            // when
            service.delete(geofenceId)

            // then
            verify { activeGeofenceRepository.deleteByGeofenceId(geofenceId) }
            verify { geofenceDefinitionRepository.deleteById(geofenceId) }
        }

        @Test
        fun `should not publish any notifications when no active trackers`() {
            // given
            every { activeGeofenceRepository.findByGeofenceId(geofenceId) } returns emptyList()
            every { geofenceDefinitionRepository.deleteById(geofenceId) } returns true

            // when
            service.delete(geofenceId)

            // then
            verify(exactly = 0) { notificationService.publish(any()) }
        }
    }

    @Nested
    inner class EvaluateLocation {

        private val trackerId = TrackerId("tracker-123")
        private val geofenceId = GeofenceId.randomGeofenceId()

        @Test
        fun `should acquire lock on tracker before processing`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns emptyList()
            every { transitionEvaluatorService.evaluateTransitions(any(), any(), any(), any()) } returns emptyList()

            // when
            service.evaluateLocation(tracker)

            // then
            verify { trackerRepository.ensureExists(trackerId) }
            verify {
                trackerRepository.findByIdForUpdate(trackerId)
            }
        }

        @Test
        fun `should always save location update for history`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns emptyList()
            every { transitionEvaluatorService.evaluateTransitions(any(), any(), any(), any()) } returns emptyList()

            // when
            service.evaluateLocation(tracker)

            // then
            verify { locationUpdateRepository.insert(tracker) }
        }

        @Test
        fun `should delegate transition evaluation to TransitionEvaluatorService`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            val activeGeofence = ActiveGeofenceEntity(trackerId, geofenceId, fixedInstant)
            val geofenceEntity = mockk<GeofenceDefinitionEntity>()
            val geofence = createGeofence()

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns listOf(activeGeofence)
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns listOf(geofenceEntity)
            every { geofenceEntityMapper.toDomain(geofenceEntity) } returns geofence
            every { transitionEvaluatorService.evaluateTransitions(any(), any(), any(), any()) } returns emptyList()

            // when
            service.evaluateLocation(tracker)

            // then
            verify {
                transitionEvaluatorService.evaluateTransitions(
                    activeGeofences = listOf(activeGeofence),
                    geofenceCandidates = listOf(geofence),
                    tracker = tracker,
                    now = fixedInstant
                )
            }
        }

        @Test
        fun `should record entry in repository when transition is ENTERED`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            val enteredTransition = Transition.entered(geofenceId, trackerId, fixedInstant)

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns emptyList()
            every { transitionEvaluatorService.evaluateTransitions(any(), any(), any(), any()) } returns listOf(enteredTransition)

            // when
            service.evaluateLocation(tracker)

            // then
            verify {
                activeGeofenceRepository.geofenceEntered(
                    ActiveGeofenceEntity(trackerId, geofenceId, fixedInstant)
                )
            }
        }

        @Test
        fun `should record exit in repository when transition is EXITED`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            val exitedTransition = Transition.exited(geofenceId, trackerId, fixedInstant)

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns emptyList()
            every { transitionEvaluatorService.evaluateTransitions(any(), any(), any(), any()) } returns listOf(exitedTransition)

            // when
            service.evaluateLocation(tracker)

            // then
            verify {
                activeGeofenceRepository.geofenceExited(
                    ActiveGeofenceEntity(trackerId, geofenceId, fixedInstant)
                )
            }
        }

        @Test
        fun `should publish notification for each transition`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)
            val geofenceId2 = GeofenceId.randomGeofenceId()
            val enteredTransition = Transition.entered(geofenceId, trackerId, fixedInstant)
            val exitedTransition = Transition.exited(geofenceId2, trackerId, fixedInstant)

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns emptyList()
            every { transitionEvaluatorService.evaluateTransitions(any(), any(), any(), any()) } returns listOf(enteredTransition, exitedTransition)

            // when
            service.evaluateLocation(tracker)

            // then
            verify { notificationService.publish(enteredTransition) }
            verify { notificationService.publish(exitedTransition) }
        }

        @Test
        fun `should not record anything when no transitions detected`() {
            // given
            val tracker = createTracker(lat = 52.0, lng = 21.0)

            every { activeGeofenceRepository.findActiveGeofences(tracker) } returns emptyList()
            every { geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng) } returns emptyList()
            every { transitionEvaluatorService.evaluateTransitions(any(), any(), any(), any()) } returns emptyList()

            // when
            service.evaluateLocation(tracker)

            // then
            verify(exactly = 0) { activeGeofenceRepository.geofenceEntered(any()) }
            verify(exactly = 0) { activeGeofenceRepository.geofenceExited(any()) }
            verify(exactly = 0) { notificationService.publish(any()) }
        }

        private fun createTracker(lat: Double, lng: Double) = Tracker(
            trackerId = trackerId,
            latlng = LatLng(lat, lng),
            timestamp = fixedInstant
        )

        private fun createGeofence() = Geofence(
            geofenceId = geofenceId,
            name = "Test Geofence",
            geometry = mockk<GeofenceGeometry>(),
            createdAt = fixedInstant
        )
    }
}
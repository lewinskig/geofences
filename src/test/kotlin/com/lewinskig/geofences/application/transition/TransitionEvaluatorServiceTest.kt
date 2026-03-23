package com.lewinskig.geofences.application.transition

import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.geometry.GeofenceGeometryFactory
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.application.tracker.TrackerId
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class TransitionEvaluatorServiceTest @Autowired constructor(
    private val transitionEvaluatorService: TransitionEvaluatorService,
    private val geofenceGeometryFactory: GeofenceGeometryFactory
) {
    private val now = Instant.parse("2026-03-23T12:00:00Z")
    private val trackerId = TrackerId("tracker-1")

    // Geofence: prostokąt od (0,0) do (10,10)
    private val squarePolygon = listOf(
        LatLng(0.0, 0.0),
        LatLng(0.0, 10.0),
        LatLng(10.0, 10.0),
        LatLng(10.0, 0.0)
    )

    private fun createGeofence(
        geofenceId: GeofenceId = GeofenceId.randomGeofenceId(),
        polygon: List<LatLng> = squarePolygon
    ): Geofence {
        return Geofence(
            geofenceId = geofenceId,
            name = "Test Geofence",
            geometry = geofenceGeometryFactory.createGeofenceGeometry(polygon),
            createdAt = now.minusSeconds(3600)
        )
    }

    private fun createTracker(lat: Double, lng: Double): Tracker {
        return Tracker(
            trackerId = trackerId,
            latlng = LatLng(lat, lng),
            timestamp = now
        )
    }

    private fun createActiveGeofence(geofenceId: GeofenceId): ActiveGeofenceEntity {
        return ActiveGeofenceEntity(
            trackId = trackerId,
            geofenceId = geofenceId,
            enteredAt = now.minusSeconds(60)
        )
    }

    @Nested
    inner class EnterTransitions {

        @Test
        fun `should emit ENTERED when tracker enters geofence for the first time`() {
            // given
            val geofence = createGeofence()
            val tracker = createTracker(lat = 5.0, lng = 5.0) // wewnątrz geofence
            val activeGeofences = emptyList<ActiveGeofenceEntity>() // brak aktywnych

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = activeGeofences,
                geofenceCandidates = listOf(geofence),
                tracker = tracker,
                now = now
            )

            // then
            assertEquals(1, transitions.size)
            val transition = transitions.single()
            assertEquals(geofence.geofenceId, transition.geofenceId)
            assertEquals(trackerId, transition.trackerId)
            assertEquals(TransitionType.ENTERED, transition.type)
            assertEquals(now, transition.timestamp)
        }

        @Test
        fun `should emit multiple ENTERED transitions when tracker enters multiple geofences`() {
            // given
            val geofence1 = createGeofence()
            val geofence2 = createGeofence(
                polygon = listOf(
                    LatLng(3.0, 3.0),
                    LatLng(3.0, 7.0),
                    LatLng(7.0, 7.0),
                    LatLng(7.0, 3.0)
                )
            )
            val tracker = createTracker(lat = 5.0, lng = 5.0) // wewnątrz obu geofence'ów

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = emptyList(),
                geofenceCandidates = listOf(geofence1, geofence2),
                tracker = tracker,
                now = now
            )

            // then
            assertEquals(2, transitions.size)
            assertTrue(transitions.all { it.type == TransitionType.ENTERED })
            assertTrue(transitions.map { it.geofenceId }.containsAll(listOf(geofence1.geofenceId, geofence2.geofenceId)))
        }

        @Test
        fun `should NOT emit ENTERED when tracker is in bbox but outside actual polygon`() {
            // given - trójkąt
            val triangleGeofence = createGeofence(
                polygon = listOf(
                    LatLng(0.0, 5.0),
                    LatLng(10.0, 0.0),
                    LatLng(10.0, 10.0)
                )
            )
            // Tracker w bbox (0-10, 0-10) ale poza trójkątem
            val tracker = createTracker(lat = 1.0, lng = 1.0)

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = emptyList(),
                geofenceCandidates = listOf(triangleGeofence),
                tracker = tracker,
                now = now
            )

            // then
            assertTrue(transitions.isEmpty())
        }
    }

    @Nested
    inner class ExitTransitions {

        @Test
        fun `should emit EXITED when tracker leaves geofence completely (outside bbox)`() {
            // given
            val geofence = createGeofence()
            val activeGeofence = createActiveGeofence(geofence.geofenceId)
            val tracker = createTracker(lat = 50.0, lng = 50.0) // daleko poza geofence

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = listOf(activeGeofence),
                geofenceCandidates = emptyList(), // brak kandydatów = poza bbox
                tracker = tracker,
                now = now
            )

            // then
            assertEquals(1, transitions.size)
            val transition = transitions.single()
            assertEquals(geofence.geofenceId, transition.geofenceId)
            assertEquals(TransitionType.EXITED, transition.type)
        }

        @Test
        fun `should emit EXITED when tracker is in bbox but exits polygon`() {
            // given - trójkąt
            val triangleGeofence = createGeofence(
                polygon = listOf(
                    LatLng(0.0, 5.0),
                    LatLng(10.0, 0.0),
                    LatLng(10.0, 10.0)
                )
            )
            val activeGeofence = createActiveGeofence(triangleGeofence.geofenceId)
            // Tracker w bbox ale poza trójkątem
            val tracker = createTracker(lat = 1.0, lng = 1.0)

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = listOf(activeGeofence),
                geofenceCandidates = listOf(triangleGeofence), // jest w bbox, więc jest kandydatem
                tracker = tracker,
                now = now
            )

            // then
            assertEquals(1, transitions.size)
            assertEquals(TransitionType.EXITED, transitions.single().type)
        }

        @Test
        fun `should emit multiple EXITED when tracker leaves multiple geofences`() {
            // given
            val geofence1 = createGeofence()
            val geofence2 = createGeofence()
            val tracker = createTracker(lat = 50.0, lng = 50.0) // poza wszystkimi

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = listOf(
                    createActiveGeofence(geofence1.geofenceId),
                    createActiveGeofence(geofence2.geofenceId)
                ),
                geofenceCandidates = emptyList(),
                tracker = tracker,
                now = now
            )

            // then
            assertEquals(2, transitions.size)
            assertTrue(transitions.all { it.type == TransitionType.EXITED })
        }
    }

    @Nested
    inner class NoTransitions {

        @Test
        fun `should NOT emit any transition when tracker stays inside geofence`() {
            // given
            val geofence = createGeofence()
            val activeGeofence = createActiveGeofence(geofence.geofenceId)
            val tracker = createTracker(lat = 5.0, lng = 5.0) // nadal wewnątrz

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = listOf(activeGeofence),
                geofenceCandidates = listOf(geofence),
                tracker = tracker,
                now = now
            )

            // then
            assertTrue(transitions.isEmpty())
        }

        @Test
        fun `should NOT emit any transition when tracker stays outside all geofences`() {
            // given
            val geofence = createGeofence()
            val tracker = createTracker(lat = 50.0, lng = 50.0) // poza geofence

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = emptyList(),
                geofenceCandidates = emptyList(), // nie jest kandydatem bo poza bbox
                tracker = tracker,
                now = now
            )

            // then
            assertTrue(transitions.isEmpty())
        }

        @Test
        fun `should NOT emit any transition when there are no geofences`() {
            // given
            val tracker = createTracker(lat = 5.0, lng = 5.0)

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = emptyList(),
                geofenceCandidates = emptyList(),
                tracker = tracker,
                now = now
            )

            // then
            assertTrue(transitions.isEmpty())
        }
    }

    @Nested
    inner class MixedTransitions {

        @Test
        fun `should emit ENTERED and EXITED when tracker moves from one geofence to another`() {
            // given
            val leftGeofence = createGeofence(
                polygon = listOf(
                    LatLng(0.0, 0.0),
                    LatLng(0.0, 5.0),
                    LatLng(10.0, 5.0),
                    LatLng(10.0, 0.0)
                )
            )
            val rightGeofence = createGeofence(
                polygon = listOf(
                    LatLng(0.0, 6.0),
                    LatLng(0.0, 12.0),
                    LatLng(10.0, 12.0),
                    LatLng(10.0, 6.0)
                )
            )
            // Tracker był w leftGeofence, teraz jest w rightGeofence
            val tracker = createTracker(lat = 5.0, lng = 9.0)

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = listOf(createActiveGeofence(leftGeofence.geofenceId)),
                geofenceCandidates = listOf(rightGeofence), // tylko right jest kandydatem
                tracker = tracker,
                now = now
            )

            // then
            assertEquals(2, transitions.size)
            
            val exitTransition = transitions.find { it.type == TransitionType.EXITED }
            val enterTransition = transitions.find { it.type == TransitionType.ENTERED }
            
            assertNotNull(exitTransition)
            assertNotNull(enterTransition)
            assertEquals(leftGeofence.geofenceId, exitTransition!!.geofenceId)
            assertEquals(rightGeofence.geofenceId, enterTransition!!.geofenceId)
        }

        @Test
        fun `should handle complex scenario with multiple enters and exits`() {
            // given
            val geofenceA = createGeofence() // tracker opuszcza
            val geofenceB = createGeofence() // tracker zostaje
            val geofenceC = createGeofence() // tracker wchodzi
            
            val tracker = createTracker(lat = 5.0, lng = 5.0) // wewnątrz B i C

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = listOf(
                    createActiveGeofence(geofenceA.geofenceId),
                    createActiveGeofence(geofenceB.geofenceId)
                ),
                geofenceCandidates = listOf(geofenceB, geofenceC), // A nie jest kandydatem (poza bbox)
                tracker = tracker,
                now = now
            )

            // then
            assertEquals(2, transitions.size)
            
            val exitedIds = transitions.filter { it.type == TransitionType.EXITED }.map { it.geofenceId }
            val enteredIds = transitions.filter { it.type == TransitionType.ENTERED }.map { it.geofenceId }
            
            assertEquals(listOf(geofenceA.geofenceId), exitedIds)
            assertEquals(listOf(geofenceC.geofenceId), enteredIds)
        }
    }

    @Nested
    inner class EdgeCases {

        @Test
        fun `should treat tracker on geofence boundary as outside`() {
            // given
            val geofence = createGeofence()
            val tracker = createTracker(lat = 0.0, lng = 5.0) // dokładnie na krawędzi

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = emptyList(),
                geofenceCandidates = listOf(geofence),
                tracker = tracker,
                now = now
            )

            // then - JTS contains() traktuje punkty na krawędzi jako NA ZEWNĄTRZ
            assertTrue(transitions.isEmpty())
        }

        @Test
        fun `should treat tracker on geofence vertex as outside`() {
            // given
            val geofence = createGeofence()
            val tracker = createTracker(lat = 0.0, lng = 0.0) // dokładnie na wierzchołku

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = emptyList(),
                geofenceCandidates = listOf(geofence),
                tracker = tracker,
                now = now
            )

            // then - JTS contains() traktuje punkty na wierzchołku jako NA ZEWNĄTRZ
            assertTrue(transitions.isEmpty())
        }

        @Test
        fun `should preserve tracker id in all transitions`() {
            // given
            val customTrackerId = TrackerId("custom-tracker-42")
            val geofence = createGeofence()
            val tracker = Tracker(
                trackerId = customTrackerId,
                latlng = LatLng(5.0, 5.0),
                timestamp = now
            )

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = emptyList(),
                geofenceCandidates = listOf(geofence),
                tracker = tracker,
                now = now
            )

            // then
            assertTrue(transitions.all { it.trackerId == customTrackerId })
        }

        @Test
        fun `should use provided timestamp for all transitions`() {
            // given
            val specificTime = Instant.parse("2026-01-15T08:30:00Z")
            val geofence = createGeofence()
            val tracker = createTracker(lat = 5.0, lng = 5.0)

            // when
            val transitions = transitionEvaluatorService.evaluateTransitions(
                activeGeofences = emptyList(),
                geofenceCandidates = listOf(geofence),
                tracker = tracker,
                now = specificTime
            )

            // then
            assertTrue(transitions.all { it.timestamp == specificTime })
        }
    }
}
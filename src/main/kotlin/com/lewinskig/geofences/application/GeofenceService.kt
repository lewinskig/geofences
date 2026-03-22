package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.transition.GeofenceTransition
import com.lewinskig.geofences.application.notification.NotificationService
import com.lewinskig.geofences.application.transition.TransitionType.ENTERED
import com.lewinskig.geofences.application.transition.TransitionType.EXITED
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceEntity
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceEntityMapper
import com.lewinskig.geofences.storage.locationupdate.LocationUpdateRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class GeofenceService @Autowired constructor(
    val geofenceDefinitionRepository: GeofenceDefinitionRepository,
    val geofenceEntityMapper: GeofenceEntityMapper,
    val locationUpdateRepository: LocationUpdateRepository,
    val activeGeofenceRepository: ActiveGeofenceRepository,
    val notificationService: NotificationService,
    val clock: Clock
) {
    fun createNew(geofence: Geofence) {
        val entity = geofenceEntityMapper.toEntity(geofence)
        geofenceDefinitionRepository.insert(entity)
    }

    fun evaluateLocation(tracker: Tracker) {

        locationUpdateRepository.insert(tracker)

        val activeGeofences = activeGeofenceRepository.findActiveGeofences(tracker)
            .associateBy(ActiveGeofenceEntity::geofenceId)

        val geofenceCandidates = geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng)
            .map { geofenceEntityMapper.toDomain(it) }
            .associateBy(Geofence::geofenceId)

        evaluateTransitions(
            activeGeofences = activeGeofences,
            geofenceCandidates = geofenceCandidates,
            tracker = tracker,
            now = clock.instant()
        )
            .forEach { transition ->
                when (transition.type) {
                    ENTERED -> {
                        activeGeofenceRepository.geofenceEntered(
                            ActiveGeofenceEntity(
                                trackId = transition.trackerId,
                                geofenceId = transition.geofenceId,
                                enteredAt = transition.timestamp
                            )
                        )
                    }

                    EXITED -> {
                        activeGeofenceRepository.geofenceExited(
                            trackId = transition.trackerId,
                            geofenceId = transition.geofenceId
                        )
                    }
                }
                notificationService.publish(transition)
            }
    }

    private fun evaluateTransitions(
        activeGeofences: Map<GeofenceId, ActiveGeofenceEntity>,
        geofenceCandidates: Map<GeofenceId, Geofence>,
        tracker: Tracker,
        now: Instant
    ): List<GeofenceTransition> {
        val trackerId = tracker.trackerId

        val activeIds = activeGeofences.keys
        val candidateIds = geofenceCandidates.keys

        val activeOnlyIds = activeIds subtract candidateIds
        val candidateAndActiveIds = candidateIds intersect activeIds
        val candidateOnlyIds = candidateIds subtract activeIds

        val exitsOutsideBbox = activeOnlyIds.map { geofenceId ->
            GeofenceTransition.exited(geofenceId, trackerId, now)
        }

        val exitsInsideBbox = candidateAndActiveIds.mapNotNull { geofenceId ->
            when (geofenceCandidates.getValue(geofenceId).containsTracker(tracker)) {
                false -> GeofenceTransition.exited(geofenceId, trackerId, now)
                else -> null
            }
        }

        val enters = candidateOnlyIds.mapNotNull { geofenceId ->
            when (geofenceCandidates.getValue(geofenceId).containsTracker(tracker)) {
                true -> GeofenceTransition.entered(geofenceId, trackerId, now)
                else -> null
            }
        }

        return exitsOutsideBbox + exitsInsideBbox + enters
    }
}
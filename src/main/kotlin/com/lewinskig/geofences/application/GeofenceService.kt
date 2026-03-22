package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.transition.Transition
import com.lewinskig.geofences.application.notification.NotificationService
import com.lewinskig.geofences.application.transition.TransitionType.ENTERED
import com.lewinskig.geofences.application.transition.TransitionType.EXITED
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.application.transition.TransitionEvaluatorService
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
    val transitionEvaluatorService: TransitionEvaluatorService,
    val clock: Clock
) {
    fun createNew(geofence: Geofence) {
        val entity = geofenceEntityMapper.toEntity(geofence)
        geofenceDefinitionRepository.insert(entity)
    }

    fun evaluateLocation(tracker: Tracker) {

        locationUpdateRepository.insert(tracker)

        val activeGeofences = activeGeofenceRepository.findActiveGeofences(tracker)

        val geofenceCandidates = geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng)
            .map { geofenceEntityMapper.toDomain(it) }

        val now = clock.instant()

        transitionEvaluatorService.evaluateTransitions(activeGeofences, geofenceCandidates, tracker, now)
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
}
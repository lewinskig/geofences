package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.notification.NotificationService
import com.lewinskig.geofences.application.transition.TransitionType.ENTERED
import com.lewinskig.geofences.application.transition.TransitionType.EXITED
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.application.transition.Transition
import com.lewinskig.geofences.application.transition.TransitionEvaluatorService
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceEntity
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceEntityMapper
import com.lewinskig.geofences.storage.locationupdate.LocationUpdateRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Clock

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
    fun create(geofence: Geofence) =
        geofenceEntityMapper.toEntity(geofence)
            .let(geofenceDefinitionRepository::insert)

    fun getAll(): List<Geofence> =
        geofenceDefinitionRepository.findAll()
            .map(geofenceEntityMapper::toDomain)

    fun delete(geofenceId: GeofenceId): Boolean {
        activeGeofenceRepository.findByGeofenceId(geofenceId)
            .forEach { activeGeofence ->
                val exitTransition = Transition.exited(
                    geofenceId = activeGeofence.geofenceId,
                    trackerId = activeGeofence.trackId,
                    timestamp = clock.instant()
                )
                notificationService.publish(exitTransition)
            }
        activeGeofenceRepository.deleteByGeofenceId(geofenceId)
        return geofenceDefinitionRepository.deleteById(geofenceId)
    }

    fun evaluateLocation(tracker: Tracker) {
        locationUpdateRepository.insert(tracker)

        val activeGeofences = activeGeofenceRepository.findActiveGeofences(tracker)

        val geofenceCandidates = geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng)
            .map { geofenceEntityMapper.toDomain(it) }

        transitionEvaluatorService.evaluateTransitions(
            activeGeofences = activeGeofences,
            geofenceCandidates = geofenceCandidates,
            tracker = tracker,
            now = clock.instant()
        )
            .forEach { transition ->
                when (transition.type) {
                    ENTERED -> activeGeofenceRepository::geofenceEntered
                    EXITED -> activeGeofenceRepository::geofenceExited
                }(
                    ActiveGeofenceEntity(
                        trackId = transition.trackerId,
                        geofenceId = transition.geofenceId,
                        enteredAt = transition.timestamp
                    )
                )
                notificationService.publish(transition)
            }
    }
}
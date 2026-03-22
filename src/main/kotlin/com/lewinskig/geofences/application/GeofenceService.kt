package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.geofence.GeofenceId
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.application.tracker.TrackerId
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceEntity
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceEntityMapper
import com.lewinskig.geofences.storage.locationupdate.LocationUpdateRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class GeofenceService @Autowired constructor(
    val geofenceDefinitionRepository: GeofenceDefinitionRepository,
    val geofenceEntityMapper: GeofenceEntityMapper,
    val locationUpdateRepository: LocationUpdateRepository,
    val activeGeofenceRepository: ActiveGeofenceRepository,
    val clock: Clock
) {
    private val logger = LoggerFactory.getLogger(GeofenceService::class.java)

    fun createNew(geofence: Geofence) {
        val entity = geofenceEntityMapper.toEntity(geofence)
        geofenceDefinitionRepository.insert(entity)
    }

    fun evaluateLocation(tracker: Tracker) {
        locationUpdateRepository.insert(tracker)

        val activeGeofences = activeGeofenceRepository.findActiveGeofences(tracker)
            .associateBy(ActiveGeofenceEntity::geofenceId)

        geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng)
            .map { geofenceEntityMapper.toDomain(it) }
            .forEach { geofenceCandidate ->
                val isInsideGeofence = geofenceCandidate.containsTracker(tracker)
                val isActive = activeGeofences.containsKey(geofenceCandidate.geofenceId)

                evaluateTransition(
                    isInsideGeofence,
                    isActive,
                    tracker.trackerId,
                    geofenceCandidate.geofenceId
                )
            }
    }

    private fun evaluateTransition(
        isInsideGeofence: Boolean,
        isActive: Boolean,
        trackId: TrackerId,
        geofenceId: GeofenceId
    ) = when {
        isInsideGeofence and isActive.not() -> {
            val activeGeofence = ActiveGeofenceEntity(
                trackId = trackId,
                geofenceId = geofenceId,
                enteredAt = clock.instant()
            )

            with(activeGeofence) {
                logger.info("Tracker {} entered geofence {} at {}", trackId, geofenceId, enteredAt)
            }
            activeGeofenceRepository.geofenceEntered(activeGeofence)
        }

        isInsideGeofence.not() and isActive -> {
            activeGeofenceRepository.geofenceExited(trackId, geofenceId)
        }

        else -> {/*Either false positive or we are still inside - do nothing*/
        }
    }
}
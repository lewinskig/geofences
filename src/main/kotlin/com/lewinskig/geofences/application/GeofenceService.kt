package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.geofencedefinition.GeofenceEntityMapper
import com.lewinskig.geofences.storage.locationupdate.LocationUpdateRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GeofenceService @Autowired constructor(
    val geofenceDefinitionRepository: GeofenceDefinitionRepository,
    val geofenceEntityMapper: GeofenceEntityMapper,
    val locationUpdateRepository: LocationUpdateRepository,
) {
    private val logger = LoggerFactory.getLogger(GeofenceService::class.java)

    fun createNew(geofence: Geofence) {
        val entity = geofenceEntityMapper.toEntity(geofence)
        geofenceDefinitionRepository.insert(entity)
    }

    fun evaluateLocation(tracker: Tracker) {
        locationUpdateRepository.insert(tracker)

        geofenceDefinitionRepository.findByPointInEnvelope(tracker.latlng)
            .map { geofenceEntityMapper.toDomain(it) }
            .forEach { geofence: Geofence ->
                if (geofence.containsTracker(tracker)) {
                    logger.info("Tracker {} is inside geofence {}", tracker.trackId, geofence.geofenceId)
                }
            }
    }
}
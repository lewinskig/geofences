package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.storage.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.GeofenceEntityMapper
import com.lewinskig.geofences.storage.LocationUpdateRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GeofenceService @Autowired constructor(
    val geofenceDefinitionRepository: GeofenceDefinitionRepository,
    val geofenceEntityMapper: GeofenceEntityMapper,
    val locationUpdateRepository: LocationUpdateRepository,
) {
    fun createNew(geofence: Geofence) {
        val entity = geofenceEntityMapper.toEntity(geofence)
        geofenceDefinitionRepository.insert(entity)
    }

    fun evaluateLocation(track: Tracker) {
        locationUpdateRepository.insert(track)
    }
}
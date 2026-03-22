package com.lewinskig.geofences.application

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.storage.GeofenceDefinitionRepository
import com.lewinskig.geofences.storage.GeofenceEntityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GeofenceService @Autowired constructor(
    val repository: GeofenceDefinitionRepository,
    val geofenceEntityMapper: GeofenceEntityMapper,
) {
    fun createNew(geofence: Geofence) {
        val entity = geofenceEntityMapper.toEntity(geofence)
        repository.insert(entity)
    }
}
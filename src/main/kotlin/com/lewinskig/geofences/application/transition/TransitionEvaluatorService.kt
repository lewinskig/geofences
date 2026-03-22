package com.lewinskig.geofences.application.transition

import com.lewinskig.geofences.application.geofence.Geofence
import com.lewinskig.geofences.application.tracker.Tracker
import com.lewinskig.geofences.storage.activegeofence.ActiveGeofenceEntity
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TransitionEvaluatorService {

    fun evaluateTransitions(
        activeGeofences: List<ActiveGeofenceEntity>,
        geofenceCandidates: List<Geofence>,
        tracker: Tracker,
        now: Instant
    ): List<Transition> {
        val trackerId = tracker.trackerId

        val activeGeofencesById = activeGeofences
            .associateBy(ActiveGeofenceEntity::geofenceId)
        val activeIds = activeGeofencesById.keys

        val geofenceCandidatesById = geofenceCandidates
            .associateBy(Geofence::geofenceId)
        val candidateIds = geofenceCandidatesById.keys

        val activeOnlyIds = activeIds subtract candidateIds
        val candidateAndActiveIds = candidateIds intersect activeIds
        val candidateOnlyIds = candidateIds subtract activeIds

        val exitsOutsideBbox = activeOnlyIds.map { geofenceId ->
            Transition.exited(geofenceId, trackerId, now)
        }

        val exitsInsideBbox = candidateAndActiveIds.mapNotNull { geofenceId ->
            when (geofenceCandidatesById.getValue(geofenceId).containsTracker(tracker)) {
                false -> Transition.exited(geofenceId, trackerId, now)
                else -> null
            }
        }

        val enters = candidateOnlyIds.mapNotNull { geofenceId ->
            when (geofenceCandidatesById.getValue(geofenceId).containsTracker(tracker)) {
                true -> Transition.entered(geofenceId, trackerId, now)
                else -> null
            }
        }

        return exitsOutsideBbox + exitsInsideBbox + enters
    }
}
package com.lewinskig.geofences.api

import com.lewinskig.geofences.application.LatLng
import com.lewinskig.geofences.application.tracker.TrackerId
import com.lewinskig.geofences.storage.tracker.TrackerEntity
import com.lewinskig.geofences.storage.tracker.TrackerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class TrackerController @Autowired constructor(
    private val trackerRepository: TrackerRepository
) {
    @GetMapping("/tracker/{id}")
    fun findTrackerById(@PathVariable("id") trackerId: TrackerId): ResponseEntity<LastSeenTrackerDto> {
        trackerRepository.ensureExists(trackerId)
        val lastSeenTracker = trackerRepository.findByIdForUpdate(trackerId)

        return if (
            lastSeenTracker.lastSeenLatitude == null ||
            lastSeenTracker.lastSeenLongitude == null ||
            lastSeenTracker.lastRecordedAt == null ||
            lastSeenTracker.lastReceivedAt == null
        ) {
            ResponseEntity.notFound().build()
        } else {
            ResponseEntity.ok(
                LastSeenTrackerDto(
                    id = lastSeenTracker.trackerId.id,
                    latlng = LatLng(lastSeenTracker.lastSeenLatitude, lastSeenTracker.lastSeenLongitude),
                    lastRecordedAt = lastSeenTracker.lastRecordedAt,
                    lastReceivedAt = lastSeenTracker.lastReceivedAt
                )
            )
        }
    }
}

data class LastSeenTrackerDto(
    val id: String,
    val latlng: LatLng,
    val lastRecordedAt: Instant,
    val lastReceivedAt: Instant
)

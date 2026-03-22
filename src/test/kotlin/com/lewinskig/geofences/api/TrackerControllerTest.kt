package com.lewinskig.geofences.api

import com.lewinskig.geofences.application.tracker.TrackerId
import com.lewinskig.geofences.storage.tracker.TrackerEntity
import com.lewinskig.geofences.storage.tracker.TrackerRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrackerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var trackerRepository: TrackerRepository

    @Test
    fun `should return 404 when tracker has no location data`() {
        // given
        val trackerId = "new-tracker-${System.nanoTime()}"

        // when & then
        mockMvc.get("/tracker/$trackerId")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return tracker last seen location when data exists`() {
        // given
        val trackerId = TrackerId("tracker-with-location-${System.nanoTime()}")
        val lastRecordedAt = Instant.parse("2026-03-22T10:00:00Z")
        val lastReceivedAt = Instant.parse("2026-03-22T10:00:01Z")

        trackerRepository.ensureExists(trackerId)
        trackerRepository.updateLastSeen(
            TrackerEntity(
                trackerId = trackerId,
                lastSeenLatitude = 52.2297,
                lastSeenLongitude = 21.0122,
                lastRecordedAt = lastRecordedAt,
                lastReceivedAt = lastReceivedAt
            )
        )

        // when & then
        mockMvc.get("/tracker/${trackerId.id}")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(trackerId.id) }
                jsonPath("$.latlng.lat") { value(52.2297) }
                jsonPath("$.latlng.lng") { value(21.0122) }
                jsonPath("$.lastRecordedAt") { value("2026-03-22T10:00:00Z") }
                jsonPath("$.lastReceivedAt") { value("2026-03-22T10:00:01Z") }
            }
    }

    @Test
    fun `should return 404 when tracker has partial location data - missing latitude`() {
        // given
        val trackerId = TrackerId("partial-tracker-${System.nanoTime()}")
        trackerRepository.ensureExists(trackerId)
        trackerRepository.updateLastSeen(
            TrackerEntity(
                trackerId = trackerId,
                lastSeenLatitude = null,
                lastSeenLongitude = 21.0122,
                lastRecordedAt = Instant.parse("2026-03-22T10:00:00Z"),
                lastReceivedAt = Instant.parse("2026-03-22T10:00:01Z")
            )
        )

        // when & then
        mockMvc.get("/tracker/${trackerId.id}")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should return latest tracker location after multiple updates`() {
        // given
        val trackerId = TrackerId("multi-update-tracker-${System.nanoTime()}")
        trackerRepository.ensureExists(trackerId)

        // First update
        trackerRepository.updateLastSeen(
            TrackerEntity(
                trackerId = trackerId,
                lastSeenLatitude = 50.0,
                lastSeenLongitude = 20.0,
                lastRecordedAt = Instant.parse("2026-03-22T09:00:00Z"),
                lastReceivedAt = Instant.parse("2026-03-22T09:00:01Z")
            )
        )

        // Second update (newer location)
        val lastRecordedAt = Instant.parse("2026-03-22T11:00:00Z")
        val lastReceivedAt = Instant.parse("2026-03-22T11:00:01Z")
        trackerRepository.updateLastSeen(
            TrackerEntity(
                trackerId = trackerId,
                lastSeenLatitude = 51.5074,
                lastSeenLongitude = -0.1278,
                lastRecordedAt = lastRecordedAt,
                lastReceivedAt = lastReceivedAt
            )
        )

        // when & then
        mockMvc.get("/tracker/${trackerId.id}")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(trackerId.id) }
                jsonPath("$.latlng.lat") { value(51.5074) }
                jsonPath("$.latlng.lng") { value(-0.1278) }
                jsonPath("$.lastRecordedAt") { value("2026-03-22T11:00:00Z") }
                jsonPath("$.lastReceivedAt") { value("2026-03-22T11:00:01Z") }
            }
    }
}
package com.lewinskig.geofences.storage.tracker

import com.lewinskig.geofences.application.tracker.TrackerId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class TrackerRepositoryTest {

    @Autowired
    private lateinit var repository: TrackerRepository

    @Nested
    inner class EnsureExists {

        @Test
        fun `should create tracker if not exists`() {
            // given
            val trackerId = TrackerId("new-tracker-${System.nanoTime()}")

            // when
            repository.ensureExists(trackerId)

            // then
            assertDoesNotThrow {
                repository.findByIdForUpdate(trackerId)
            }
        }

        @Test
        fun `should not fail when tracker already exists`() {
            // given
            val trackerId = TrackerId("existing-tracker-${System.nanoTime()}")
            repository.ensureExists(trackerId)

            // when & then
            assertDoesNotThrow { repository.ensureExists(trackerId) }
        }

        @Test
        fun `should be idempotent - multiple calls should succeed`() {
            // given
            val trackerId = TrackerId("idempotent-tracker-${System.nanoTime()}")

            // when & then
            repeat(5) {
                assertDoesNotThrow { repository.ensureExists(trackerId) }
            }
        }
    }

    @Nested
    inner class FindByIdForUpdate {

        @Test
        fun `should acquire lock and return tracker entity`() {
            // given
            val trackerId = TrackerId("lock-tracker-${System.nanoTime()}")
            repository.ensureExists(trackerId)

            // when
            val result = repository.findByIdForUpdate(trackerId)

            // then
            assertEquals(trackerId, result.trackerId)
            assertNull(result.lastSeenLatitude)
            assertNull(result.lastSeenLongitude)
            assertNull(result.lastRecordedAt)
            assertNull(result.lastReceivedAt)
        }

        @Test
        fun `should throw when tracker does not exist`() {
            // given
            val nonExistentTrackerId = TrackerId("non-existent-${System.nanoTime()}")

            // when & then
            assertThrows(Exception::class.java) {
                repository.findByIdForUpdate(nonExistentTrackerId)
            }
        }
    }

    @Nested
    inner class UpdateLastSeen {

        @Test
        fun `should update tracker last seen location`() {
            // given
            val trackerId = TrackerId("update-tracker-${System.nanoTime()}")
            repository.ensureExists(trackerId)

            val lastRecordedAt = Instant.parse("2026-03-22T10:00:00Z")
            val lastReceivedAt = Instant.parse("2026-03-22T10:00:01Z")
            val entity = TrackerEntity(
                trackerId = trackerId,
                lastSeenLatitude = 52.2297,
                lastSeenLongitude = 21.0122,
                lastRecordedAt = lastRecordedAt,
                lastReceivedAt = lastReceivedAt
            )

            // when
            repository.updateLastSeen(entity)

            // then
            val updated = repository.findByIdForUpdate(trackerId)
            assertEquals(52.2297, updated.lastSeenLatitude)
            assertEquals(21.0122, updated.lastSeenLongitude)
            assertEquals(lastRecordedAt, updated.lastRecordedAt)
            assertEquals(lastReceivedAt, updated.lastReceivedAt)
        }

        @Test
        fun `should overwrite previous last seen location`() {
            // given
            val trackerId = TrackerId("overwrite-tracker-${System.nanoTime()}")
            repository.ensureExists(trackerId)

            val initialEntity = TrackerEntity(
                trackerId = trackerId,
                lastSeenLatitude = 50.0,
                lastSeenLongitude = 20.0,
                lastRecordedAt = Instant.parse("2026-03-22T09:00:00Z"),
                lastReceivedAt = Instant.parse("2026-03-22T09:00:01Z")
            )
            repository.updateLastSeen(initialEntity)

            val newRecordedAt = Instant.parse("2026-03-22T11:00:00Z")
            val newReceivedAt = Instant.parse("2026-03-22T11:00:01Z")
            val newEntity = TrackerEntity(
                trackerId = trackerId,
                lastSeenLatitude = 51.5074,
                lastSeenLongitude = -0.1278,
                lastRecordedAt = newRecordedAt,
                lastReceivedAt = newReceivedAt
            )

            // when
            repository.updateLastSeen(newEntity)

            // then
            val updated = repository.findByIdForUpdate(trackerId)
            assertEquals(51.5074, updated.lastSeenLatitude)
            assertEquals(-0.1278, updated.lastSeenLongitude)
            assertEquals(newRecordedAt, updated.lastRecordedAt)
            assertEquals(newReceivedAt, updated.lastReceivedAt)
        }
    }
}
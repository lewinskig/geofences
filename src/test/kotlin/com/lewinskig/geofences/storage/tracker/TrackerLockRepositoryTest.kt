package com.lewinskig.geofences.storage.tracker

import com.lewinskig.geofences.application.tracker.TrackerId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class TrackerLockRepositoryTest {

    @Autowired
    private lateinit var repository: TrackerLockRepository

    @Nested
    inner class EnsureExists {

        @Test
        fun `should create tracker if not exists`() {
            // given
            val trackerId = TrackerId("new-tracker-${System.nanoTime()}")

            // when
            repository.ensureExists(trackerId)

            // then
            assertDoesNotThrow { repository.lock(trackerId) }
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
    inner class Lock {

        @Test
        fun `should acquire lock on existing tracker`() {
            // given
            val trackerId = TrackerId("lock-tracker-${System.nanoTime()}")
            repository.ensureExists(trackerId)

            // when & then
            assertDoesNotThrow { repository.lock(trackerId) }
        }

        @Test
        fun `should throw when tracker does not exist`() {
            // given
            val nonExistentTrackerId = TrackerId("non-existent-${System.nanoTime()}")

            // when & then
            assertThrows(Exception::class.java) {
                repository.lock(nonExistentTrackerId)
            }
        }
    }
}
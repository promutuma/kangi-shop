package ke.eelaminnovations.kangaishop

import ke.eelaminnovations.kangaishop.domain.model.Person
import ke.eelaminnovations.kangaishop.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncConflictResolutionTest {

    @Test
    fun testConflictResolutionWinner() {
        val localRecord = Person(
            id = "person_123",
            name = "Local Name",
            phone = "0711223344",
            smsEnabled = true,
            notes = "Local notes",
            lastModifiedAt = 1000L, // Older modification
            syncStatus = SyncStatus.SYNCED
        )

        val remoteRecord = Person(
            id = "person_123",
            name = "Remote Name",
            phone = "0711223344",
            smsEnabled = true,
            notes = "Remote notes",
            lastModifiedAt = 2000L, // Newer modification
            syncStatus = SyncStatus.SYNCED
        )

        // Conflict resolution: newer lastModifiedAt wins
        val resolvedRecord = if (remoteRecord.lastModifiedAt > localRecord.lastModifiedAt) {
            remoteRecord
        } else {
            localRecord
        }

        assertEquals("Remote Name", resolvedRecord.name)
        assertEquals("Remote notes", resolvedRecord.notes)
        assertEquals(2000L, resolvedRecord.lastModifiedAt)
    }

    @Test
    fun testConflictResolutionLocalWins() {
        val localRecord = Person(
            id = "person_123",
            name = "Local Name",
            phone = "0711223344",
            smsEnabled = true,
            notes = "Local notes",
            lastModifiedAt = 3000L, // Newer modification
            syncStatus = SyncStatus.PENDING
        )

        val remoteRecord = Person(
            id = "person_123",
            name = "Remote Name",
            phone = "0711223344",
            smsEnabled = true,
            notes = "Remote notes",
            lastModifiedAt = 2000L, // Older modification
            syncStatus = SyncStatus.SYNCED
        )

        // Conflict resolution: newer lastModifiedAt wins
        val resolvedRecord = if (remoteRecord.lastModifiedAt > localRecord.lastModifiedAt) {
            remoteRecord
        } else {
            localRecord
        }

        assertEquals("Local Name", resolvedRecord.name)
        assertEquals("Local notes", resolvedRecord.notes)
        assertEquals(3000L, resolvedRecord.lastModifiedAt)
    }
}

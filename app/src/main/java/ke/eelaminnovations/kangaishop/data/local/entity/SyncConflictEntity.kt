package ke.eelaminnovations.kangaishop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_conflicts")
data class SyncConflictEntity(
    @PrimaryKey val id: String,
    val entityType: String, // "PERSON", "DELIVERY", "TRANSACTION"
    val entityId: String,
    val title: String,
    val localContent: String,  // JSON or summary of local values
    val remoteContent: String, // JSON or summary of remote values
    val resolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

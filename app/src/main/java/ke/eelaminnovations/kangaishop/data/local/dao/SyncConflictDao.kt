package ke.eelaminnovations.kangaishop.data.local.dao

import androidx.room.*
import ke.eelaminnovations.kangaishop.data.local.entity.SyncConflictEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncConflictDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: SyncConflictEntity)

    @Query("SELECT * FROM sync_conflicts WHERE resolved = 0 ORDER BY createdAt DESC")
    fun getUnresolvedConflicts(): Flow<List<SyncConflictEntity>>

    @Query("UPDATE sync_conflicts SET resolved = 1 WHERE id = :id")
    suspend fun markResolved(id: String)

    @Query("DELETE FROM sync_conflicts WHERE id = :id")
    suspend fun deleteConflict(id: String)
}

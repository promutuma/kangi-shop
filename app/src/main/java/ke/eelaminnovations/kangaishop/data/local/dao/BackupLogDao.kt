package ke.eelaminnovations.kangaishop.data.local.dao

import androidx.room.*
import ke.eelaminnovations.kangaishop.data.local.entity.BackupLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupLogDao {
    @Query("SELECT * FROM backup_log ORDER BY backupDate DESC")
    fun getAllBackupLogs(): Flow<List<BackupLogEntity>>

    @Query("SELECT * FROM backup_log ORDER BY backupDate DESC LIMIT 1")
    suspend fun getLatestBackup(): BackupLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupLog(log: BackupLogEntity)

    @Query("DELETE FROM backup_log WHERE id NOT IN (SELECT id FROM backup_log ORDER BY backupDate DESC LIMIT 30)")
    suspend fun pruneOldLogs()
}

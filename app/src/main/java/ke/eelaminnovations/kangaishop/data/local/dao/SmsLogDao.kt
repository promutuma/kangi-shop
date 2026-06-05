package ke.eelaminnovations.kangaishop.data.local.dao

import androidx.room.*
import ke.eelaminnovations.kangaishop.data.local.entity.SmsLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsLogDao {
    @Query("SELECT * FROM sms_log ORDER BY sentAt DESC")
    fun getAllSmsLogs(): Flow<List<SmsLogEntity>>

    @Query("SELECT * FROM sms_log WHERE status = 'FAILED' ORDER BY sentAt DESC")
    fun getFailedSmsLogs(): Flow<List<SmsLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsLog(log: SmsLogEntity)

    @Update
    suspend fun updateSmsLog(log: SmsLogEntity)

    @Query("UPDATE sms_log SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, errorMessage: String?)
}

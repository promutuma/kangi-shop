package ke.eelaminnovations.kangaishop.domain.repository

import ke.eelaminnovations.kangaishop.domain.model.SmsLog
import kotlinx.coroutines.flow.Flow

interface SmsLogRepository {
    fun getAllSmsLogs(): Flow<List<SmsLog>>
    fun getFailedSmsLogs(): Flow<List<SmsLog>>
    suspend fun insertSmsLog(log: SmsLog)
    suspend fun updateSmsLog(log: SmsLog)
}

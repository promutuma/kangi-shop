package ke.eelaminnovations.kangaishop.data.repository

import ke.eelaminnovations.kangaishop.data.local.dao.SmsLogDao
import ke.eelaminnovations.kangaishop.data.local.entity.toEntity
import ke.eelaminnovations.kangaishop.domain.model.SmsLog
import ke.eelaminnovations.kangaishop.domain.repository.SmsLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SmsLogRepositoryImpl @Inject constructor(
    private val dao: SmsLogDao
) : SmsLogRepository {

    override fun getAllSmsLogs(): Flow<List<SmsLog>> =
        dao.getAllSmsLogs().map { it.map { e -> e.toDomain() } }

    override fun getFailedSmsLogs(): Flow<List<SmsLog>> =
        dao.getFailedSmsLogs().map { it.map { e -> e.toDomain() } }

    override suspend fun insertSmsLog(log: SmsLog) =
        dao.insertSmsLog(log.toEntity())

    override suspend fun updateSmsLog(log: SmsLog) =
        dao.updateSmsLog(log.toEntity())
}

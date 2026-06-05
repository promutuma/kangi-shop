package ke.eelaminnovations.kangaishop.data.repository

import ke.eelaminnovations.kangaishop.data.local.dao.MilkDeliveryDao
import ke.eelaminnovations.kangaishop.data.local.entity.toEntity
import ke.eelaminnovations.kangaishop.domain.model.MilkDelivery
import ke.eelaminnovations.kangaishop.domain.model.MilkSession
import ke.eelaminnovations.kangaishop.domain.repository.MilkDeliveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MilkDeliveryRepositoryImpl @Inject constructor(
    private val dao: MilkDeliveryDao
) : MilkDeliveryRepository {

    override fun getDeliveriesForDate(dateEpoch: Long): Flow<List<MilkDelivery>> {
        val startOfDay = startOfDay(dateEpoch)
        val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1)
        return dao.getDeliveriesForDate(startOfDay, endOfDay).map { it.map { e -> e.toDomain() } }
    }

    override fun getDeliveriesForPerson(personId: String): Flow<List<MilkDelivery>> =
        dao.getDeliveriesForPerson(personId).map { it.map { e -> e.toDomain() } }

    override fun getDeliveriesForPersonInRange(personId: String, startDate: Long, endDate: Long): Flow<List<MilkDelivery>> =
        dao.getDeliveriesForPersonInRange(personId, startDate, endDate).map { it.map { e -> e.toDomain() } }

    override fun getDeliveryForPersonSessionDate(personId: String, session: MilkSession, dateEpoch: Long): Flow<MilkDelivery?> {
        val startOfDay = startOfDay(dateEpoch)
        val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1)
        return dao.getDeliveryForPersonSessionDate(personId, session.name, startOfDay, endOfDay)
            .map { it?.toDomain() }
    }

    override fun getAllDeliveriesInRange(startDate: Long, endDate: Long): Flow<List<MilkDelivery>> =
        dao.getAllDeliveriesInRange(startDate, endDate).map { it.map { e -> e.toDomain() } }

    override suspend fun insertDelivery(delivery: MilkDelivery) =
        dao.insertDelivery(delivery.toEntity())

    override suspend fun updateDelivery(delivery: MilkDelivery) =
        dao.updateDelivery(delivery.toEntity())

    override suspend fun deleteDelivery(id: String) =
        dao.softDeleteDelivery(id)

    override suspend fun getPendingSync(): List<MilkDelivery> =
        dao.getPendingSync().map { it.toDomain() }

    private fun startOfDay(epochMs: Long): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}

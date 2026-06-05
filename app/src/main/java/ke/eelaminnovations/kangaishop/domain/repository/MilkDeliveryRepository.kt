package ke.eelaminnovations.kangaishop.domain.repository

import ke.eelaminnovations.kangaishop.domain.model.MilkDelivery
import ke.eelaminnovations.kangaishop.domain.model.MilkSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MilkDeliveryRepository {
    fun getDeliveriesForDate(dateEpoch: Long): Flow<List<MilkDelivery>>
    fun getDeliveriesForPerson(personId: String): Flow<List<MilkDelivery>>
    fun getDeliveriesForPersonInRange(personId: String, startDate: Long, endDate: Long): Flow<List<MilkDelivery>>
    fun getDeliveryForPersonSessionDate(personId: String, session: MilkSession, dateEpoch: Long): Flow<MilkDelivery?>
    fun getAllDeliveriesInRange(startDate: Long, endDate: Long): Flow<List<MilkDelivery>>
    suspend fun insertDelivery(delivery: MilkDelivery)
    suspend fun updateDelivery(delivery: MilkDelivery)
    suspend fun deleteDelivery(id: String)
    suspend fun getPendingSync(): List<MilkDelivery>
}

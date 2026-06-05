package ke.eelaminnovations.kangaishop.data.local.dao

import androidx.room.*
import ke.eelaminnovations.kangaishop.data.local.entity.MilkDeliveryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDeliveryDao {
    @Query("""
        SELECT * FROM milk_deliveries
        WHERE deliveryDate >= :startOfDay AND deliveryDate < :endOfDay AND isDeleted = 0
        ORDER BY personId, session
    """)
    fun getDeliveriesForDate(startOfDay: Long, endOfDay: Long): Flow<List<MilkDeliveryEntity>>

    @Query("SELECT * FROM milk_deliveries WHERE personId = :personId AND isDeleted = 0 ORDER BY deliveryDate DESC")
    fun getDeliveriesForPerson(personId: String): Flow<List<MilkDeliveryEntity>>

    @Query("""
        SELECT * FROM milk_deliveries
        WHERE personId = :personId AND deliveryDate >= :startDate AND deliveryDate <= :endDate AND isDeleted = 0
        ORDER BY deliveryDate DESC
    """)
    fun getDeliveriesForPersonInRange(personId: String, startDate: Long, endDate: Long): Flow<List<MilkDeliveryEntity>>

    @Query("""
        SELECT * FROM milk_deliveries
        WHERE personId = :personId AND session = :session AND deliveryDate >= :startOfDay AND deliveryDate < :endOfDay AND isDeleted = 0
        LIMIT 1
    """)
    fun getDeliveryForPersonSessionDate(personId: String, session: String, startOfDay: Long, endOfDay: Long): Flow<MilkDeliveryEntity?>

    @Query("""
        SELECT * FROM milk_deliveries
        WHERE deliveryDate >= :startDate AND deliveryDate <= :endDate AND isDeleted = 0
        ORDER BY deliveryDate DESC
    """)
    fun getAllDeliveriesInRange(startDate: Long, endDate: Long): Flow<List<MilkDeliveryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: MilkDeliveryEntity)

    @Update
    suspend fun updateDelivery(delivery: MilkDeliveryEntity)

    @Query("UPDATE milk_deliveries SET isDeleted = 1, lastModifiedAt = :now WHERE id = :id")
    suspend fun softDeleteDelivery(id: String, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM milk_deliveries WHERE syncStatus = 'PENDING' AND isDeleted = 0")
    suspend fun getPendingSync(): List<MilkDeliveryEntity>

    @Query("UPDATE milk_deliveries SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM milk_deliveries WHERE id = :id LIMIT 1")
    suspend fun getDeliveryById(id: String): MilkDeliveryEntity?

    @Query("SELECT COUNT(*) FROM milk_deliveries WHERE syncStatus = 'PENDING' AND isDeleted = 0")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT * FROM milk_deliveries")
    suspend fun getAllDeliveriesForBackup(): List<MilkDeliveryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveries(deliveries: List<MilkDeliveryEntity>)
}





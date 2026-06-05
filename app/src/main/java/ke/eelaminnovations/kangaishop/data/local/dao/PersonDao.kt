package ke.eelaminnovations.kangaishop.data.local.dao

import androidx.room.*
import ke.eelaminnovations.kangaishop.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people WHERE isDeleted = 0 ORDER BY name")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE isDeleted = 0 AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name")
    fun searchPeople(query: String): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id AND isDeleted = 0")
    suspend fun getPersonById(id: String): PersonEntity?

    @Query("SELECT * FROM people WHERE phone = :phone AND isDeleted = 0 LIMIT 1")
    suspend fun getPersonByPhone(phone: String): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Query("UPDATE people SET isDeleted = 1, lastModifiedAt = :now WHERE id = :id")
    suspend fun softDeletePerson(id: String, now: Long = System.currentTimeMillis())

    @Query("""
        SELECT DISTINCT p.* FROM people p
        INNER JOIN milk_deliveries m ON p.id = m.personId
        WHERE p.isDeleted = 0 AND m.isDeleted = 0
        ORDER BY p.name
    """)
    fun getPeopleWithDeliveries(): Flow<List<PersonEntity>>

    @Query("""
        SELECT DISTINCT p.* FROM people p
        INNER JOIN ledger_transactions t ON p.id = t.personId
        WHERE p.isDeleted = 0 AND t.isDeleted = 0 AND t.type = 'CREDIT_ISSUED'
        ORDER BY p.name
    """)
    fun getPeopleWithCredit(): Flow<List<PersonEntity>>

    @Query("SELECT COUNT(*) FROM people WHERE syncStatus = 'PENDING' AND isDeleted = 0")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT * FROM people WHERE syncStatus = 'PENDING' AND isDeleted = 0")
    suspend fun getPendingSync(): List<PersonEntity>

    @Query("SELECT * FROM people")
    suspend fun getAllPeopleForBackup(): List<PersonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(people: List<PersonEntity>)
}





package ke.eelaminnovations.kangaishop.data.local.dao

import androidx.room.*
import ke.eelaminnovations.kangaishop.data.local.entity.AppUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUserDao {
    @Query("SELECT * FROM app_users WHERE isActive = 1 ORDER BY name")
    fun getAllUsers(): Flow<List<AppUserEntity>>

    @Query("SELECT * FROM app_users WHERE id = :id")
    suspend fun getUserById(id: String): AppUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUserEntity)

    @Update
    suspend fun updateUser(user: AppUserEntity)

    @Query("UPDATE app_users SET isActive = 0 WHERE id = :id")
    suspend fun deactivateUser(id: String)

    @Query("SELECT pin FROM app_users WHERE id = :userId")
    suspend fun getPinForUser(userId: String): String?

    @Query("SELECT * FROM app_users")
    suspend fun getAllUsersForBackup(): List<AppUserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<AppUserEntity>)
}

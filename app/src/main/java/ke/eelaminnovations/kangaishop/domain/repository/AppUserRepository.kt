package ke.eelaminnovations.kangaishop.domain.repository

import ke.eelaminnovations.kangaishop.domain.model.AppUser
import ke.eelaminnovations.kangaishop.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AppUserRepository {
    fun getAllUsers(): Flow<List<AppUser>>
    suspend fun getUserById(id: String): AppUser?
    suspend fun insertUser(user: AppUser)
    suspend fun updateUser(user: AppUser)
    suspend fun deactivateUser(id: String)
    suspend fun verifyPin(userId: String, pin: String): Boolean
}

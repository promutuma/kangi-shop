package ke.eelaminnovations.kangaishop.data.repository

import ke.eelaminnovations.kangaishop.data.local.dao.AppUserDao
import ke.eelaminnovations.kangaishop.data.local.entity.toEntity
import ke.eelaminnovations.kangaishop.domain.model.AppUser
import ke.eelaminnovations.kangaishop.domain.repository.AppUserRepository
import ke.eelaminnovations.kangaishop.utils.hashPin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppUserRepositoryImpl @Inject constructor(
    private val dao: AppUserDao
) : AppUserRepository {

    override fun getAllUsers(): Flow<List<AppUser>> =
        dao.getAllUsers().map { it.map { e -> e.toDomain() } }

    override suspend fun getUserById(id: String): AppUser? =
        dao.getUserById(id)?.toDomain()

    override suspend fun insertUser(user: AppUser) {
        val hashed = user.copy(pin = hashPin(user.pin))
        dao.insertUser(hashed.toEntity())
    }

    override suspend fun updateUser(user: AppUser) =
        dao.updateUser(user.toEntity())

    override suspend fun deactivateUser(id: String) =
        dao.deactivateUser(id)

    override suspend fun verifyPin(userId: String, pin: String): Boolean {
        val storedPin = dao.getPinForUser(userId) ?: return false
        return storedPin == hashPin(pin)
    }
}

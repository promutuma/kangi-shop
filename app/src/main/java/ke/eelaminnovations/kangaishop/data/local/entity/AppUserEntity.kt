package ke.eelaminnovations.kangaishop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.eelaminnovations.kangaishop.domain.model.AppUser
import ke.eelaminnovations.kangaishop.domain.model.UserRole

@Entity(tableName = "app_users")
data class AppUserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val pin: String,
    val role: String,
    val isActive: Boolean
) {
    fun toDomain() = AppUser(
        id = id,
        name = name,
        phone = phone,
        pin = pin,
        role = UserRole.valueOf(role),
        isActive = isActive
    )
}

fun AppUser.toEntity() = AppUserEntity(
    id = id,
    name = name,
    phone = phone,
    pin = pin,
    role = role.name,
    isActive = isActive
)

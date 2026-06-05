package ke.eelaminnovations.kangaishop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.eelaminnovations.kangaishop.domain.model.Person
import ke.eelaminnovations.kangaishop.domain.model.PersonRole
import ke.eelaminnovations.kangaishop.domain.model.SyncStatus

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val role: String,
    val smsEnabled: Boolean,
    val notes: String,
    val createdAt: Long,
    val lastModifiedAt: Long,
    val isDeleted: Boolean,
    val syncStatus: String,
    val deviceId: String
) {
    fun toDomain() = Person(
        id = id,
        name = name,
        phone = phone,
        role = PersonRole.valueOf(role),
        smsEnabled = smsEnabled,
        notes = notes,
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt,
        isDeleted = isDeleted,
        syncStatus = SyncStatus.valueOf(syncStatus),
        deviceId = deviceId
    )
}

fun Person.toEntity() = PersonEntity(
    id = id,
    name = name,
    phone = phone,
    role = role.name,
    smsEnabled = smsEnabled,
    notes = notes,
    createdAt = createdAt,
    lastModifiedAt = lastModifiedAt,
    isDeleted = isDeleted,
    syncStatus = syncStatus.name,
    deviceId = deviceId
)

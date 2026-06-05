package ke.eelaminnovations.kangaishop.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ke.eelaminnovations.kangaishop.domain.model.MilkDelivery
import ke.eelaminnovations.kangaishop.domain.model.MilkQuality
import ke.eelaminnovations.kangaishop.domain.model.MilkSession
import ke.eelaminnovations.kangaishop.domain.model.SyncStatus

@Entity(
    tableName = "milk_deliveries",
    foreignKeys = [ForeignKey(
        entity = PersonEntity::class,
        parentColumns = ["id"],
        childColumns = ["personId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("personId"), Index("deliveryDate"), Index("session")]
)
data class MilkDeliveryEntity(
    @PrimaryKey val id: String,
    val personId: String,
    val deliveryDate: Long,
    val session: String,
    val litres: Double,
    val pricePerLitre: Double,
    val totalValue: Double,
    val quality: String,
    val rejectedLitres: Double,
    val notes: String,
    val recordedBy: String,
    val createdAt: Long,
    val lastModifiedAt: Long,
    val isDeleted: Boolean,
    val syncStatus: String,
    val deviceId: String
) {
    fun toDomain() = MilkDelivery(
        id = id,
        personId = personId,
        deliveryDate = deliveryDate,
        session = MilkSession.valueOf(session),
        litres = litres,
        pricePerLitre = pricePerLitre,
        totalValue = totalValue,
        quality = MilkQuality.valueOf(quality),
        rejectedLitres = rejectedLitres,
        notes = notes,
        recordedBy = recordedBy,
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt,
        isDeleted = isDeleted,
        syncStatus = SyncStatus.valueOf(syncStatus),
        deviceId = deviceId
    )
}

fun MilkDelivery.toEntity() = MilkDeliveryEntity(
    id = id,
    personId = personId,
    deliveryDate = deliveryDate,
    session = session.name,
    litres = litres,
    pricePerLitre = pricePerLitre,
    totalValue = totalValue,
    quality = quality.name,
    rejectedLitres = rejectedLitres,
    notes = notes,
    recordedBy = recordedBy,
    createdAt = createdAt,
    lastModifiedAt = lastModifiedAt,
    isDeleted = isDeleted,
    syncStatus = syncStatus.name,
    deviceId = deviceId
)

package ke.eelaminnovations.kangaishop.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ke.eelaminnovations.kangaishop.domain.model.LedgerTransaction
import ke.eelaminnovations.kangaishop.domain.model.SyncStatus
import ke.eelaminnovations.kangaishop.domain.model.TransactionDirection
import ke.eelaminnovations.kangaishop.domain.model.TransactionType

@Entity(
    tableName = "ledger_transactions",
    foreignKeys = [ForeignKey(
        entity = PersonEntity::class,
        parentColumns = ["id"],
        childColumns = ["personId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("personId"), Index("transactionDate"), Index("type"), Index("parentTransactionId")]
)
data class LedgerTransactionEntity(
    @PrimaryKey val id: String,
    val personId: String,
    val type: String,
    val direction: String,
    val amount: Double,
    val milkDeliveryId: String?,
    val goodsDescription: String?,
    val mpesaRef: String?,
    val transactionDate: Long,
    val runningBalance: Double,
    val parentTransactionId: String?,
    val smsSent: Boolean,
    val notes: String?,
    val recordedBy: String,
    val createdAt: Long,
    val lastModifiedAt: Long,
    val isDeleted: Boolean,
    val syncStatus: String,
    val deviceId: String
) {
    fun toDomain() = LedgerTransaction(
        id = id,
        personId = personId,
        type = TransactionType.valueOf(type),
        direction = TransactionDirection.valueOf(direction),
        amount = amount,
        milkDeliveryId = milkDeliveryId,
        goodsDescription = goodsDescription,
        mpesaRef = mpesaRef,
        transactionDate = transactionDate,
        runningBalance = runningBalance,
        parentTransactionId = parentTransactionId,
        smsSent = smsSent,
        notes = notes,
        recordedBy = recordedBy,
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt,
        isDeleted = isDeleted,
        syncStatus = SyncStatus.valueOf(syncStatus),
        deviceId = deviceId
    )
}

fun LedgerTransaction.toEntity() = LedgerTransactionEntity(
    id = id,
    personId = personId,
    type = type.name,
    direction = direction.name,
    amount = amount,
    milkDeliveryId = milkDeliveryId,
    goodsDescription = goodsDescription,
    mpesaRef = mpesaRef,
    transactionDate = transactionDate,
    runningBalance = runningBalance,
    parentTransactionId = parentTransactionId,
    smsSent = smsSent,
    notes = notes,
    recordedBy = recordedBy,
    createdAt = createdAt,
    lastModifiedAt = lastModifiedAt,
    isDeleted = isDeleted,
    syncStatus = syncStatus.name,
    deviceId = deviceId
)

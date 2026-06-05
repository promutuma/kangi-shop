package ke.eelaminnovations.kangaishop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.eelaminnovations.kangaishop.domain.model.SmsLog
import ke.eelaminnovations.kangaishop.domain.model.SmsStatus

@Entity(tableName = "sms_log")
data class SmsLogEntity(
    @PrimaryKey val id: String,
    val recipientPhone: String,
    val recipientName: String,
    val message: String,
    val status: String,
    val sentAt: Long,
    val relatedTransactionId: String?,
    val errorMessage: String?
) {
    fun toDomain() = SmsLog(
        id = id,
        recipientPhone = recipientPhone,
        recipientName = recipientName,
        message = message,
        status = SmsStatus.valueOf(status),
        sentAt = sentAt,
        relatedTransactionId = relatedTransactionId,
        errorMessage = errorMessage
    )
}

fun SmsLog.toEntity() = SmsLogEntity(
    id = id,
    recipientPhone = recipientPhone,
    recipientName = recipientName,
    message = message,
    status = status.name,
    sentAt = sentAt,
    relatedTransactionId = relatedTransactionId,
    errorMessage = errorMessage
)

package ke.eelaminnovations.kangaishop.domain.model

data class SmsLog(
    val id: String,
    val recipientPhone: String,
    val recipientName: String,
    val message: String,
    val status: SmsStatus,
    val sentAt: Long,
    val relatedTransactionId: String? = null,
    val errorMessage: String? = null
)

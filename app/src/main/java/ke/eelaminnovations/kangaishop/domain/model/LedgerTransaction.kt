package ke.eelaminnovations.kangaishop.domain.model

data class LedgerTransaction(
    val id: String,
    val personId: String,
    val type: TransactionType,
    val direction: TransactionDirection,
    val amount: Double,
    val milkDeliveryId: String? = null,
    val goodsDescription: String? = null,
    val mpesaRef: String? = null,
    val transactionDate: Long = System.currentTimeMillis(),
    val runningBalance: Double = 0.0,
    val parentTransactionId: String? = null,
    val smsSent: Boolean = false,
    val notes: String? = null,
    val recordedBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val deviceId: String = ""
)

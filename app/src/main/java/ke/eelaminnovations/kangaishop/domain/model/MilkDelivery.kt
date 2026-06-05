package ke.eelaminnovations.kangaishop.domain.model

data class MilkDelivery(
    val id: String,
    val personId: String,
    val deliveryDate: Long,
    val session: MilkSession,
    val litres: Double,
    val pricePerLitre: Double,
    val totalValue: Double = litres * pricePerLitre,
    val quality: MilkQuality = MilkQuality.GOOD,
    val rejectedLitres: Double = 0.0,
    val notes: String = "",
    val recordedBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val deviceId: String = ""
)

data class DailySupplierSummary(
    val person: Person,
    val morningDelivery: MilkDelivery?,
    val eveningDelivery: MilkDelivery?,
    val netBalance: Double
) {
    val totalLitres: Double get() = (morningDelivery?.litres ?: 0.0) + (eveningDelivery?.litres ?: 0.0)
    val totalValue: Double get() = (morningDelivery?.totalValue ?: 0.0) + (eveningDelivery?.totalValue ?: 0.0)
}

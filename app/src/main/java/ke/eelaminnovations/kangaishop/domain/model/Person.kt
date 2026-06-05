package ke.eelaminnovations.kangaishop.domain.model

data class Person(
    val id: String,
    val name: String,
    val phone: String,
    val role: PersonRole = PersonRole.CONTACT_ONLY,
    val smsEnabled: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val deviceId: String = ""
)

data class PersonWithRole(
    val person: Person,
    val role: PersonRole,
    val netBalance: Double
)

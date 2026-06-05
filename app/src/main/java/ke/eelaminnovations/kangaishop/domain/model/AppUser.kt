package ke.eelaminnovations.kangaishop.domain.model

data class AppUser(
    val id: String,
    val name: String,
    val phone: String,
    val pin: String,
    val role: UserRole,
    val isActive: Boolean
)

package ke.eelaminnovations.kangaishop.domain.model

enum class SyncStatus { PENDING, SYNCED, FAILED }

enum class UserRole { OWNER, ATTENDANT }

enum class MilkSession { MORNING, EVENING }

enum class MilkQuality { GOOD, REJECTED, PARTIAL }

enum class TransactionType {
    MILK_DELIVERY,
    PAYMENT_CASH,
    PAYMENT_MPESA,
    PAYMENT_GOODS,
    CREDIT_ISSUED,
    GOODS_ON_CREDIT,
    CUSTOMER_PAYMENT_CASH,
    CUSTOMER_PAYMENT_MPESA
}

enum class TransactionDirection {
    CREDIT,  // reduces net balance (shop pays supplier, customer pays shop)
    DEBIT    // increases net balance (milk delivered, credit issued)
}

enum class PersonRole { SUPPLIER, CUSTOMER, BOTH, CONTACT_ONLY }

enum class SmsStatus { SENT, FAILED }

enum class BackupStatus { SUCCESS, FAILED }

enum class AppTheme {
    DYNAMIC,
    BRAND_BLUE,
    FOREST_GREEN,
    SUNSET_GOLD,
    OCEAN_BREEZE,
    LAVENDER_FIELD,
    CRIMSON_VELVET,
    CHARCOAL_MINIMAL,
    SAKURA_BLOSSOM,
    MIDNIGHT_NAVY
}

package ke.eelaminnovations.kangaishop.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ke.eelaminnovations.kangaishop.data.local.dao.*
import ke.eelaminnovations.kangaishop.data.local.entity.*
import ke.eelaminnovations.kangaishop.data.local.migrations.MIGRATION_2_3

@Database(
    entities = [
        AppUserEntity::class,
        PersonEntity::class,
        MilkDeliveryEntity::class,
        LedgerTransactionEntity::class,
        SmsLogEntity::class,
        BackupLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUserDao(): AppUserDao
    abstract fun personDao(): PersonDao
    abstract fun milkDeliveryDao(): MilkDeliveryDao
    abstract fun ledgerTransactionDao(): LedgerTransactionDao
    abstract fun smsLogDao(): SmsLogDao
    abstract fun backupLogDao(): BackupLogDao
}

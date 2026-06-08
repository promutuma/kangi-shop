package ke.eelaminnovations.kangaishop.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ke.eelaminnovations.kangaishop.data.local.AppDatabase
import ke.eelaminnovations.kangaishop.data.local.dao.*
import ke.eelaminnovations.kangaishop.data.local.migrations.MIGRATION_2_3
import ke.eelaminnovations.kangaishop.data.local.migrations.MIGRATION_3_4
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "kangai_shop_db")
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()


    @Provides fun provideAppUserDao(db: AppDatabase): AppUserDao = db.appUserDao()
    @Provides fun providePersonDao(db: AppDatabase): PersonDao = db.personDao()
    @Provides fun provideMilkDeliveryDao(db: AppDatabase): MilkDeliveryDao = db.milkDeliveryDao()
    @Provides fun provideLedgerTransactionDao(db: AppDatabase): LedgerTransactionDao = db.ledgerTransactionDao()
    @Provides fun provideSmsLogDao(db: AppDatabase): SmsLogDao = db.smsLogDao()
    @Provides fun provideBackupLogDao(db: AppDatabase): BackupLogDao = db.backupLogDao()
    @Provides fun provideSyncConflictDao(db: AppDatabase): SyncConflictDao = db.syncConflictDao()
}

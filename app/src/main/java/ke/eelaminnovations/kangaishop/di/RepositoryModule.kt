package ke.eelaminnovations.kangaishop.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ke.eelaminnovations.kangaishop.data.repository.*
import ke.eelaminnovations.kangaishop.domain.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindPersonRepository(impl: PersonRepositoryImpl): PersonRepository

    @Binds @Singleton
    abstract fun bindMilkDeliveryRepository(impl: MilkDeliveryRepositoryImpl): MilkDeliveryRepository

    @Binds @Singleton
    abstract fun bindLedgerTransactionRepository(impl: LedgerTransactionRepositoryImpl): LedgerTransactionRepository

    @Binds @Singleton
    abstract fun bindAppUserRepository(impl: AppUserRepositoryImpl): AppUserRepository

    @Binds @Singleton
    abstract fun bindSmsLogRepository(impl: SmsLogRepositoryImpl): SmsLogRepository
}

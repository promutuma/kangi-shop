package ke.eelaminnovations.kangaishop.data.repository

import ke.eelaminnovations.kangaishop.data.local.dao.LedgerTransactionDao
import ke.eelaminnovations.kangaishop.data.local.entity.toEntity
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.domain.repository.LedgerTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class LedgerTransactionRepositoryImpl @Inject constructor(
    private val dao: LedgerTransactionDao
) : LedgerTransactionRepository {

    override fun getTransactionsForPerson(personId: String): Flow<List<LedgerTransaction>> =
        dao.getTransactionsForPerson(personId).map { it.map { e -> e.toDomain() } }

    override fun getTransactionsForPersonInRange(personId: String, startDate: Long, endDate: Long): Flow<List<LedgerTransaction>> =
        dao.getTransactionsForPersonInRange(personId, startDate, endDate).map { it.map { e -> e.toDomain() } }

    override fun getTransactionsByType(personId: String, types: List<TransactionType>): Flow<List<LedgerTransaction>> =
        dao.getTransactionsByType(personId, types.map { it.name }).map { it.map { e -> e.toDomain() } }

    override suspend fun getNetBalance(personId: String): Double =
        dao.getNetBalance(personId)

    override suspend fun insertTransaction(transaction: LedgerTransaction) =
        dao.insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: LedgerTransaction) =
        dao.updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(id: String) {
        val originalEntity = dao.getTransactionById(id) ?: return
        val original = originalEntity.toDomain()
        val compensating = original.copy(
            id = UUID.randomUUID().toString(),
            direction = if (original.direction == TransactionDirection.DEBIT) {
                TransactionDirection.CREDIT
            } else {
                TransactionDirection.DEBIT
            },
            parentTransactionId = original.id,
            notes = "Reversal of transaction: ${original.id.take(8)}${if (!original.notes.isNullOrBlank()) " (${original.notes})" else ""}",
            createdAt = System.currentTimeMillis(),
            lastModifiedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        dao.insertTransaction(compensating.toEntity())
    }

    override suspend fun getPendingSync(): List<LedgerTransaction> =
        dao.getPendingSync().map { it.toDomain() }

    override suspend fun hasDeliveries(personId: String): Boolean =
        dao.hasDeliveries(personId)

    override suspend fun hasCredit(personId: String): Boolean =
        dao.hasCredit(personId)

    override fun getSupplierDebts(): Flow<Map<String, Double>> =
        dao.getSupplierBalances().map { list -> list.associate { it.personId to it.balance } }

    override fun getCustomerDebts(): Flow<Map<String, Double>> =
        dao.getCustomerBalances().map { list -> list.associate { it.personId to it.balance } }

    override fun getLastPaymentDates(): Flow<Map<String, Long>> =
        dao.getLastPaymentDates().map { list -> list.associate { it.personId to it.lastDate } }

    override fun getLastCreditDates(): Flow<Map<String, Long>> =
        dao.getLastCreditDates().map { list -> list.associate { it.personId to it.lastDate } }
}

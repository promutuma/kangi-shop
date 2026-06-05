package ke.eelaminnovations.kangaishop.domain.repository

import ke.eelaminnovations.kangaishop.domain.model.LedgerTransaction
import ke.eelaminnovations.kangaishop.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface LedgerTransactionRepository {
    fun getTransactionsForPerson(personId: String): Flow<List<LedgerTransaction>>
    fun getTransactionsForPersonInRange(personId: String, startDate: Long, endDate: Long): Flow<List<LedgerTransaction>>
    fun getTransactionsByType(personId: String, types: List<TransactionType>): Flow<List<LedgerTransaction>>
    suspend fun getNetBalance(personId: String): Double
    suspend fun insertTransaction(transaction: LedgerTransaction)
    suspend fun updateTransaction(transaction: LedgerTransaction)
    suspend fun deleteTransaction(id: String)
    suspend fun getPendingSync(): List<LedgerTransaction>
    suspend fun hasDeliveries(personId: String): Boolean
    suspend fun hasCredit(personId: String): Boolean
    fun getSupplierDebts(): Flow<Map<String, Double>>
    fun getCustomerDebts(): Flow<Map<String, Double>>
    fun getLastPaymentDates(): Flow<Map<String, Long>>
    fun getLastCreditDates(): Flow<Map<String, Long>>
}

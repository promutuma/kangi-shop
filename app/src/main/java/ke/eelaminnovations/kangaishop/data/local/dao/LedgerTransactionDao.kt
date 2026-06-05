package ke.eelaminnovations.kangaishop.data.local.dao

import androidx.room.*
import ke.eelaminnovations.kangaishop.data.local.entity.LedgerTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerTransactionDao {
    @Query("""
        SELECT id, personId, type, direction, amount, milkDeliveryId, goodsDescription, mpesaRef, transactionDate,
               SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE -amount END) 
               OVER (PARTITION BY personId ORDER BY transactionDate ASC, createdAt ASC) AS runningBalance,
               parentTransactionId, smsSent, notes, recordedBy, createdAt, lastModifiedAt, isDeleted, syncStatus, deviceId
        FROM ledger_transactions
        WHERE personId = :personId AND isDeleted = 0
        ORDER BY transactionDate DESC, createdAt DESC
    """)
    fun getTransactionsForPerson(personId: String): Flow<List<LedgerTransactionEntity>>

    @Query("""
        SELECT id, personId, type, direction, amount, milkDeliveryId, goodsDescription, mpesaRef, transactionDate,
               SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE -amount END) 
               OVER (PARTITION BY personId ORDER BY transactionDate ASC, createdAt ASC) AS runningBalance,
               parentTransactionId, smsSent, notes, recordedBy, createdAt, lastModifiedAt, isDeleted, syncStatus, deviceId
        FROM ledger_transactions
        WHERE personId = :personId AND transactionDate >= :startDate AND transactionDate <= :endDate AND isDeleted = 0
        ORDER BY transactionDate DESC, createdAt DESC
    """)
    fun getTransactionsForPersonInRange(personId: String, startDate: Long, endDate: Long): Flow<List<LedgerTransactionEntity>>

    @Query("""
        SELECT id, personId, type, direction, amount, milkDeliveryId, goodsDescription, mpesaRef, transactionDate,
               SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE -amount END) 
               OVER (PARTITION BY personId ORDER BY transactionDate ASC, createdAt ASC) AS runningBalance,
               parentTransactionId, smsSent, notes, recordedBy, createdAt, lastModifiedAt, isDeleted, syncStatus, deviceId
        FROM ledger_transactions
        WHERE personId = :personId AND type IN (:types) AND isDeleted = 0
        ORDER BY transactionDate DESC, createdAt DESC
    """)
    fun getTransactionsByType(personId: String, types: List<String>): Flow<List<LedgerTransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE -amount END), 0.0)
        FROM ledger_transactions
        WHERE personId = :personId AND isDeleted = 0
    """)
    suspend fun getNetBalance(personId: String): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerTransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: LedgerTransactionEntity)

    @Query("UPDATE ledger_transactions SET isDeleted = 1, lastModifiedAt = :now WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM ledger_transactions WHERE syncStatus = 'PENDING' AND isDeleted = 0")
    suspend fun getPendingSync(): List<LedgerTransactionEntity>

    @Query("UPDATE ledger_transactions SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT COUNT(*) > 0 FROM ledger_transactions WHERE personId = :personId AND type = 'MILK_DELIVERY' AND isDeleted = 0")
    suspend fun hasDeliveries(personId: String): Boolean

    @Query("SELECT COUNT(*) > 0 FROM ledger_transactions WHERE personId = :personId AND type = 'CREDIT_ISSUED' AND isDeleted = 0")
    suspend fun hasCredit(personId: String): Boolean

    @Query("""
        SELECT personId,
        COALESCE(SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE -amount END), 0.0) as balance
        FROM ledger_transactions
        WHERE isDeleted = 0 AND type IN ('MILK_DELIVERY', 'PAYMENT_CASH', 'PAYMENT_MPESA', 'PAYMENT_GOODS')
        GROUP BY personId
    """)
    fun getSupplierBalances(): Flow<List<PersonBalance>>

    @Query("""
        SELECT personId,
        COALESCE(SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE -amount END), 0.0) as balance
        FROM ledger_transactions
        WHERE isDeleted = 0 AND type IN ('CREDIT_ISSUED', 'GOODS_ON_CREDIT', 'CUSTOMER_PAYMENT_CASH', 'CUSTOMER_PAYMENT_MPESA')
        GROUP BY personId
    """)
    fun getCustomerBalances(): Flow<List<PersonBalance>>

    @Query("SELECT * FROM ledger_transactions WHERE smsSent = 0 AND isDeleted = 0 ORDER BY createdAt DESC LIMIT 50")
    fun getUnsentSmsTransactions(): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM ledger_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): LedgerTransactionEntity?

    @Query("SELECT COUNT(*) FROM ledger_transactions WHERE syncStatus = 'PENDING' AND isDeleted = 0")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT * FROM ledger_transactions")
    suspend fun getAllTransactionsForBackup(): List<LedgerTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<LedgerTransactionEntity>)

    @Query("""
        SELECT personId, MAX(transactionDate) as lastDate
        FROM ledger_transactions
        WHERE isDeleted = 0 AND type IN ('CUSTOMER_PAYMENT_CASH', 'CUSTOMER_PAYMENT_MPESA')
        GROUP BY personId
    """)
    fun getLastPaymentDates(): Flow<List<PersonLastDate>>

    @Query("""
        SELECT personId, MAX(transactionDate) as lastDate
        FROM ledger_transactions
        WHERE isDeleted = 0 AND type = 'CREDIT_ISSUED'
        GROUP BY personId
    """)
    fun getLastCreditDates(): Flow<List<PersonLastDate>>
}

data class PersonBalance(val personId: String, val balance: Double)
data class PersonLastDate(val personId: String, val lastDate: Long)

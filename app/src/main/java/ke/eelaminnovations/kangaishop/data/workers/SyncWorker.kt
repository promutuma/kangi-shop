package ke.eelaminnovations.kangaishop.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.eelaminnovations.kangaishop.data.local.dao.LedgerTransactionDao
import ke.eelaminnovations.kangaishop.data.local.dao.MilkDeliveryDao
import ke.eelaminnovations.kangaishop.data.local.dao.PersonDao
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val personDao: PersonDao,
    private val milkDeliveryDao: MilkDeliveryDao,
    private val ledgerTransactionDao: LedgerTransactionDao,
    private val settings: AppSettingsDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val shopId = settings.shopId.first()
            if (shopId.isEmpty()) return Result.success()

            val lastSync = settings.lastSyncTime.first()
            val syncStartTime = System.currentTimeMillis()

            val firestore = FirebaseFirestore.getInstance()
            val shopRef = firestore.collection("shops").document(shopId)

            // PUSH Local Updates
            val pendingPeople = personDao.getPendingSync()
            for (person in pendingPeople) {
                shopRef.collection("people").document(person.id).set(person).await()
                personDao.insertPerson(person.copy(syncStatus = "SYNCED"))
            }

            val pendingDeliveries = milkDeliveryDao.getPendingSync()
            for (delivery in pendingDeliveries) {
                shopRef.collection("milk_deliveries").document(delivery.id).set(delivery).await()
                milkDeliveryDao.markSynced(delivery.id)
            }

            val pendingTransactions = ledgerTransactionDao.getPendingSync()
            for (tx in pendingTransactions) {
                shopRef.collection("ledger_transactions").document(tx.id).set(tx).await()
                ledgerTransactionDao.markSynced(tx.id)
            }

            // PULL Remote Updates
            val remotePeople = shopRef.collection("people")
                .whereGreaterThan("lastModifiedAt", lastSync)
                .get().await()
            for (doc in remotePeople.documents) {
                val id = doc.getString("id") ?: continue
                val name = doc.getString("name") ?: ""
                val phone = doc.getString("phone") ?: ""
                val smsEnabled = doc.getBoolean("smsEnabled") ?: true
                val notes = doc.getString("notes") ?: ""
                val createdAt = doc.getLong("createdAt") ?: 0L
                val lastModifiedAt = doc.getLong("lastModifiedAt") ?: 0L
                val isDeleted = doc.getBoolean("isDeleted") ?: false
                val role = doc.getString("role") ?: "CONTACT_ONLY"
                val syncStatus = "SYNCED"
                val deviceId = doc.getString("deviceId") ?: ""

                val remotePerson = ke.eelaminnovations.kangaishop.data.local.entity.PersonEntity(
                    id, name, phone, role, smsEnabled, notes, createdAt, lastModifiedAt, isDeleted, syncStatus, deviceId
                )
                val localPerson = personDao.getPersonById(id)
                if (localPerson == null || lastModifiedAt > localPerson.lastModifiedAt) {
                    personDao.insertPerson(remotePerson)
                }
            }

            val remoteDeliveries = shopRef.collection("milk_deliveries")
                .whereGreaterThan("lastModifiedAt", lastSync)
                .get().await()
            for (doc in remoteDeliveries.documents) {
                val id = doc.getString("id") ?: continue
                val personId = doc.getString("personId") ?: ""
                val deliveryDate = doc.getLong("deliveryDate") ?: 0L
                val session = doc.getString("session") ?: ""
                val litres = doc.getDouble("litres") ?: 0.0
                val pricePerLitre = doc.getDouble("pricePerLitre") ?: 0.0
                val totalValue = doc.getDouble("totalValue") ?: 0.0
                val quality = doc.getString("quality") ?: ""
                val rejectedLitres = doc.getDouble("rejectedLitres") ?: 0.0
                val notes = doc.getString("notes") ?: ""
                val recordedBy = doc.getString("recordedBy") ?: ""
                val createdAt = doc.getLong("createdAt") ?: 0L
                val lastModifiedAt = doc.getLong("lastModifiedAt") ?: 0L
                val isDeleted = doc.getBoolean("isDeleted") ?: false
                val syncStatus = "SYNCED"
                val deviceId = doc.getString("deviceId") ?: ""

                val remoteDelivery = ke.eelaminnovations.kangaishop.data.local.entity.MilkDeliveryEntity(
                    id, personId, deliveryDate, session, litres, pricePerLitre, totalValue, quality, rejectedLitres, notes, recordedBy, createdAt, lastModifiedAt, isDeleted, syncStatus, deviceId
                )
                val localDelivery = milkDeliveryDao.getDeliveryById(id)
                if (localDelivery == null || lastModifiedAt > localDelivery.lastModifiedAt) {
                    milkDeliveryDao.insertDelivery(remoteDelivery)
                }
            }

            val remoteTransactions = shopRef.collection("ledger_transactions")
                .whereGreaterThan("lastModifiedAt", lastSync)
                .get().await()
            for (doc in remoteTransactions.documents) {
                val id = doc.getString("id") ?: continue
                val personId = doc.getString("personId") ?: ""
                val type = doc.getString("type") ?: ""
                val direction = doc.getString("direction") ?: ""
                val amount = doc.getDouble("amount") ?: 0.0
                val milkDeliveryId = doc.getString("milkDeliveryId")
                val goodsDescription = doc.getString("goodsDescription")
                val mpesaRef = doc.getString("mpesaRef")
                val transactionDate = doc.getLong("transactionDate") ?: 0L
                val runningBalance = doc.getDouble("runningBalance") ?: 0.0
                val parentTransactionId = doc.getString("parentTransactionId")
                val smsSent = doc.getBoolean("smsSent") ?: false
                val notes = doc.getString("notes")
                val recordedBy = doc.getString("recordedBy") ?: ""
                val createdAt = doc.getLong("createdAt") ?: 0L
                val lastModifiedAt = doc.getLong("lastModifiedAt") ?: 0L
                val isDeleted = doc.getBoolean("isDeleted") ?: false
                val syncStatus = "SYNCED"
                val deviceId = doc.getString("deviceId") ?: ""

                val remoteTx = ke.eelaminnovations.kangaishop.data.local.entity.LedgerTransactionEntity(
                    id, personId, type, direction, amount, milkDeliveryId, goodsDescription, mpesaRef, transactionDate, runningBalance, parentTransactionId, smsSent, notes, recordedBy, createdAt, lastModifiedAt, isDeleted, syncStatus, deviceId
                )
                val localTx = ledgerTransactionDao.getTransactionById(id)
                if (localTx == null || lastModifiedAt > localTx.lastModifiedAt) {
                    ledgerTransactionDao.insertTransaction(remoteTx)
                }
            }

            settings.setLastSyncTime(syncStartTime)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }


    companion object {
        const val WORK_NAME = "SyncWorker"

        fun buildRequest() = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        fun buildOneTimeRequest() = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
    }
}

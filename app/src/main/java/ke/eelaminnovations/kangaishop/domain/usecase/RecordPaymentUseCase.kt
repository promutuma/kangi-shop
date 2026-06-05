package ke.eelaminnovations.kangaishop.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.domain.repository.*
import ke.eelaminnovations.kangaishop.utils.SmsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class RecordPaymentUseCase @Inject constructor(
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val personRepository: PersonRepository,
    private val smsLogRepository: SmsLogRepository,
    private val settings: AppSettingsDataStore,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        personId: String,
        type: TransactionType,
        amount: Double,
        mpesaRef: String? = null,
        goodsDescription: String? = null,
        notes: String? = null,
        recordedBy: String = ""
    ): LedgerTransaction {
        require(type in listOf(
            TransactionType.PAYMENT_CASH,
            TransactionType.PAYMENT_MPESA,
            TransactionType.PAYMENT_GOODS,
            TransactionType.CUSTOMER_PAYMENT_CASH,
            TransactionType.CUSTOMER_PAYMENT_MPESA
        ))
        val currentBalance = ledgerTransactionRepository.getNetBalance(personId)
        val newBalance = currentBalance - amount
        val transaction = LedgerTransaction(
            id = UUID.randomUUID().toString(),
            personId = personId,
            type = type,
            direction = TransactionDirection.CREDIT,
            amount = amount,
            mpesaRef = mpesaRef,
            goodsDescription = goodsDescription,
            runningBalance = newBalance,
            notes = notes,
            recordedBy = recordedBy
        )

        ledgerTransactionRepository.insertTransaction(transaction)

        withContext(Dispatchers.IO) {
            try {
                val globalSms = settings.smsEnabled.first()
                val person = personRepository.getPersonById(personId)
                if (globalSms && person != null && person.smsEnabled) {
                    val shopName = settings.shopName.first()
                    val message = SmsHelper.buildTransactionMessage(shopName, transaction, person.name)
                    val smsLogId = UUID.randomUUID().toString()
                    val sent = SmsHelper.sendSms(context, person.phone, message, smsLogId)

                    val log = SmsLog(
                        id = smsLogId,
                        recipientPhone = person.phone,
                        recipientName = person.name,
                        message = message,
                        status = if (sent) SmsStatus.SENT else SmsStatus.FAILED,
                        sentAt = System.currentTimeMillis(),
                        relatedTransactionId = transaction.id,
                        errorMessage = if (sent) null else "SmsManager send failure"
                    )
                    smsLogRepository.insertSmsLog(log)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return transaction
    }
}


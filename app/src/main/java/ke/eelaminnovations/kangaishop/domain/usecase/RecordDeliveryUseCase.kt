package ke.eelaminnovations.kangaishop.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.domain.repository.*
import ke.eelaminnovations.kangaishop.utils.SmsHelper
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class RecordDeliveryUseCase @Inject constructor(
    private val milkDeliveryRepository: MilkDeliveryRepository,
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val personRepository: PersonRepository,
    private val smsLogRepository: SmsLogRepository,
    private val settings: AppSettingsDataStore,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(delivery: MilkDelivery): LedgerTransaction {
        milkDeliveryRepository.insertDelivery(delivery)
        val currentBalance = ledgerTransactionRepository.getNetBalance(delivery.personId)
        val newBalance = currentBalance + delivery.totalValue
        val transaction = LedgerTransaction(
            id = UUID.randomUUID().toString(),
            personId = delivery.personId,
            type = TransactionType.MILK_DELIVERY,
            direction = TransactionDirection.DEBIT,
            amount = delivery.totalValue,
            milkDeliveryId = delivery.id,
            transactionDate = delivery.deliveryDate,
            runningBalance = newBalance,
            recordedBy = delivery.recordedBy
        )
        ledgerTransactionRepository.insertTransaction(transaction)

        try {
            val globalSms = settings.smsEnabled.first()
            val person = personRepository.getPersonById(delivery.personId)
            if (globalSms && person != null && person.smsEnabled) {
                val shopName = settings.shopName.first()
                val message = SmsHelper.buildMilkDeliveryMessage(shopName, delivery, newBalance, person.name)
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

        return transaction
    }
}


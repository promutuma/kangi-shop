package ke.eelaminnovations.kangaishop.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.os.Build
import android.util.Log
import ke.eelaminnovations.kangaishop.domain.model.LedgerTransaction
import ke.eelaminnovations.kangaishop.domain.model.MilkDelivery
import ke.eelaminnovations.kangaishop.domain.model.MilkSession
import ke.eelaminnovations.kangaishop.domain.model.TransactionType

object SmsHelper {

    fun buildMilkDeliveryMessage(
        shopName: String,
        delivery: MilkDelivery,
        balance: Double,
        personName: String
    ): String {
        val session = if (delivery.session == MilkSession.MORNING) "Morning" else "Evening"
        val balanceLabel = if (balance > 0) "Shop owes you ${formatKes(balance)}" else "${personName} owes shop ${formatKes(-balance)}"
        return "$shopName: $session milk received - ${formatLitres(delivery.litres)}L @ KES ${delivery.pricePerLitre}/L = ${formatKes(delivery.totalValue)}. $balanceLabel. ${formatShortDate(delivery.deliveryDate)}"
    }

    fun buildPaymentMessage(shopName: String, amount: Double, balance: Double, date: Long): String {
        val balanceLabel = if (balance > 0) "Balance: shop owes you ${formatKes(balance)}" else "Balance: you owe shop ${formatKes(-balance)}"
        return "$shopName: Payment of ${formatKes(amount)} received. $balanceLabel. ${formatShortDate(date)}"
    }

    fun buildGoodsMessage(shopName: String, amount: Double, balance: Double, date: Long): String {
        val balanceLabel = if (balance > 0) "Balance: shop owes you ${formatKes(balance)}" else "Balance: you owe shop ${formatKes(-balance)}"
        return "$shopName: Goods ${formatKes(amount)} deducted. $balanceLabel. ${formatShortDate(date)}"
    }

    fun buildCreditMessage(shopName: String, amount: Double, balance: Double, date: Long): String =
        "$shopName: Credit of ${formatKes(amount)} recorded. You owe: ${formatKes(balance)}. ${formatShortDate(date)}"

    fun buildCustomerPaymentMessage(shopName: String, amount: Double, balance: Double, date: Long): String =
        "$shopName: Payment of ${formatKes(amount)} received. You owe: ${formatKes(balance)}. Thank you! ${formatShortDate(date)}"

    fun buildTransactionMessage(
        shopName: String,
        transaction: LedgerTransaction,
        personName: String
    ): String = when (transaction.type) {
        TransactionType.MILK_DELIVERY -> "$shopName: Milk recorded ${formatKes(transaction.amount)}. Balance: ${formatKes(transaction.runningBalance)}. ${formatShortDate(transaction.transactionDate)}"
        TransactionType.PAYMENT_CASH, TransactionType.PAYMENT_MPESA -> buildPaymentMessage(shopName, transaction.amount, transaction.runningBalance, transaction.transactionDate)
        TransactionType.PAYMENT_GOODS -> buildGoodsMessage(shopName, transaction.amount, transaction.runningBalance, transaction.transactionDate)
        TransactionType.CREDIT_ISSUED -> buildCreditMessage(shopName, transaction.amount, transaction.runningBalance, transaction.transactionDate)
        TransactionType.GOODS_ON_CREDIT -> "$shopName: You purchased ${transaction.goodsDescription ?: "items"} for ${formatKes(transaction.amount)}. You owe: ${formatKes(transaction.runningBalance)}. ${formatShortDate(transaction.transactionDate)}"
        TransactionType.CUSTOMER_PAYMENT_CASH, TransactionType.CUSTOMER_PAYMENT_MPESA -> buildCustomerPaymentMessage(shopName, transaction.amount, transaction.runningBalance, transaction.transactionDate)
    }

    fun sendSms(context: Context, phoneNumber: String, message: String, smsLogId: String): Boolean {
        return try {
            Log.i("SmsHelper", "=== SMS SEND START ===")
            Log.i("SmsHelper", "Original phone: $phoneNumber")
            Log.i("SmsHelper", "Message (${message.length} chars): ${message.take(100)}")

            // Check runtime permissions
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(android.Manifest.permission.SEND_SMS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.w("SmsHelper", "❌ SEND_SMS permission not granted")
                    return false
                }
                Log.d("SmsHelper", "✅ SEND_SMS permission granted")
            }

            // Format phone number: ensure Kenyan format (remove common prefixes)
            var phone = phoneNumber.trim()
            Log.d("SmsHelper", "Trimmed phone: $phone")

            // Remove leading zeros and spaces
            phone = phone.replace(Regex("^0+"), "").replace(" ", "")
            Log.d("SmsHelper", "After removing zeros: $phone")

            // Add country code if missing
            if (!phone.startsWith("+") && !phone.startsWith("254")) {
                phone = "+254$phone"
            } else if (phone.startsWith("254") && !phone.startsWith("+")) {
                phone = "+$phone"
            }

            Log.i("SmsHelper", "✅ Formatted phone: $phone")
            Log.d("SmsHelper", "Message to send: $message")

            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            if (smsManager == null) {
                Log.e("SmsHelper", "❌ SmsManager is null - device may not support SMS")
                return false
            }

            // Diagnostic info
            val telephonyManager = context.getSystemService(android.telephony.TelephonyManager::class.java)
            val phoneStateInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                "NetworkCountryIso: ${telephonyManager?.networkCountryIso ?: "N/A"}, " +
                "CarrierName: ${telephonyManager?.simOperatorName ?: "N/A"}"
            } else {
                "SimState: ${telephonyManager?.simState ?: "N/A"}"
            }
            Log.d("SmsHelper", "Device SMS info - $phoneStateInfo")

            val intent = Intent("SMS_SENT").apply {
                putExtra("sms_log_id", smsLogId)
            }
            val sentIntent = PendingIntent.getBroadcast(
                context,
                smsLogId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (message.length > 160) {
                val parts = smsManager.divideMessage(message)
                val sentIntents = ArrayList<PendingIntent>(parts.size).apply { repeat(parts.size) { add(sentIntent) } }
                Log.d("SmsHelper", "Sending multipart SMS (${parts.size} parts)")
                smsManager.sendMultipartTextMessage(phone, null, parts, sentIntents, null)
            } else {
                Log.d("SmsHelper", "Sending single SMS to: $phone")
                smsManager.sendTextMessage(phone, null, message, sentIntent, null)
            }

            Log.i("SmsHelper", "✅ SMS queued successfully to $phone")
            true
        } catch (e: SecurityException) {
            Log.e("SmsHelper", "SecurityException: SEND_SMS permission might not be granted", e)
            false
        } catch (e: Exception) {
            Log.e("SmsHelper", "Failed to send SMS", e)
            false
        }
    }
}

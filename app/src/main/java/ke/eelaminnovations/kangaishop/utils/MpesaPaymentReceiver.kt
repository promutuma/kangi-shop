package ke.eelaminnovations.kangaishop.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

/**
 * Detects incoming M-Pesa payment confirmations and extracts transaction metadata.
 * Fired at priority 999 to intercept before other handlers.
 *
 * Pattern: "MPESA"conf code CONFIRMED. You have received KES<amount>from<sender>
 */
@Serializable
data class MpesaPayment(
    val confCode: String,        // e.g. "SA12BCD3EF4"
    val amountKes: Double,       // e.g. 3000.00
    val senderName: String,      // e.g. "Kamau Gitau"
    val senderPhone: String,     // extracted/normalized
    val timestamp: Long = System.currentTimeMillis()
)

class MpesaPaymentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (context == null) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        messages.forEach { msg ->
            val sender = msg.originatingAddress ?: return@forEach
            val body = msg.messageBody

            // Only process Safaricom M-Pesa messages
            if (!body.contains("MPESA", ignoreCase = true) || !body.contains("CONFIRMED", ignoreCase = true)) {
                return@forEach
            }

            val payment = parseMpesaMessage(body, sender)
            if (payment != null) {
                Log.i("MpesaReceiver", "Detected: KES ${payment.amountKes} from ${payment.senderName} (${payment.confCode})")
                // Post notification / update LiveData for UI to consume
                // (In production, this would trigger a WorkManager job or post to a ViewModel)
            }
        }
    }

    companion object {
        /**
         * Extracts M-Pesa payment details from SMS body.
         * Expects format: "MPESA conf SA1BC2D3E4 Confirmed. You have received KES3,000.00 from KAMAU GITAU."
         */
        fun parseMpesaMessage(body: String, senderPhone: String): MpesaPayment? {
            return try {
                // Regex: extract confirmation code (10 alphanumerics after "conf ")
                val confRegex = Regex("""conf\s+([A-Z0-9]{10})""")
                val confMatch = confRegex.find(body) ?: return null
                val confCode = confMatch.groupValues[1]

                // Regex: extract amount (digits + optional comma + decimals)
                val amountRegex = Regex("""KES\s*([\d,]+(?:\.\d{2})?)""")
                val amountMatch = amountRegex.find(body) ?: return null
                val amountStr = amountMatch.groupValues[1].replace(",", "")
                val amountKes = amountStr.toDoubleOrNull() ?: return null

                // Regex: extract sender name (word characters + spaces after "from ")
                val nameRegex = Regex("""from\s+([A-Z\s]+)[\.\n]?$""", RegexOption.MULTILINE)
                val nameMatch = nameRegex.find(body) ?: return null
                val senderName = nameMatch.groupValues[1].trim()

                MpesaPayment(
                    confCode = confCode,
                    amountKes = amountKes,
                    senderName = senderName,
                    senderPhone = senderPhone
                )
            } catch (e: Exception) {
                Log.e("MpesaReceiver", "Failed to parse M-Pesa message", e)
                null
            }
        }
    }
}

package ke.eelaminnovations.kangaishop.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ke.eelaminnovations.kangaishop.data.local.dao.SmsLogDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSentReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SmsLogDaoProvider {
        fun smsLogDao(): SmsLogDao
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "SMS_SENT") {
            val smsLogId = intent.getStringExtra("sms_log_id") ?: return
            val resultCode = resultCode
            val status = if (resultCode == Activity.RESULT_OK) "SENT" else "FAILED"
            val errorMessage = when (resultCode) {
                Activity.RESULT_OK -> null
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic failure - likely no SIM/service"
                SmsManager.RESULT_ERROR_NO_SERVICE -> "No network service available"
                SmsManager.RESULT_ERROR_NULL_PDU -> "Invalid PDU - message format error"
                SmsManager.RESULT_ERROR_RADIO_OFF -> "Device radio is off (airplane mode?)"
                else -> "Error code: $resultCode"
            }

            Log.i("SmsSentReceiver", "═══ SMS_SENT RESULT ═══")
            Log.i("SmsSentReceiver", "Log ID: $smsLogId")
            Log.i("SmsSentReceiver", "Status: $status")
            if (errorMessage != null) {
                Log.w("SmsSentReceiver", "Error: $errorMessage")
                Log.w("SmsSentReceiver", "🔍 Diagnostic: Check if device has active SIM card and mobile service")
            } else {
                Log.i("SmsSentReceiver", "✅ SMS delivered successfully")
            }
            Log.i("SmsSentReceiver", "═══════════════════════")

            try {
                val entryPoint = EntryPointAccessors.fromApplication(context, SmsLogDaoProvider::class.java)
                val smsLogDao = entryPoint.smsLogDao()
                CoroutineScope(Dispatchers.IO).launch {
                    smsLogDao.updateStatus(smsLogId, status, errorMessage)
                }
            } catch (e: Exception) {
                Log.e("SmsSentReceiver", "Failed to access smsLogDao", e)
            }
        }
    }
}

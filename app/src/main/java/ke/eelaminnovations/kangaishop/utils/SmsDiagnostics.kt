package ke.eelaminnovations.kangaishop.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log

object SmsDiagnostics {
    fun diagnoseSmsProblem(context: Context) {
        Log.d("SmsDiagnostics", "═══ SMS DIAGNOSTIC REPORT ═══")

        // 1. Check SEND_SMS permission
        val sendSmsGranted = context.checkSelfPermission(android.Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED
        Log.d("SmsDiagnostics", "1. SEND_SMS permission granted: $sendSmsGranted")

        // 2. Check if device has telephony
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val hasTelephony = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        Log.d("SmsDiagnostics", "2. Has telephony feature: $hasTelephony")

        // 3. Check SIM state
        if (telephonyManager != null) {
            val simState = telephonyManager.simState
            val simStateLabel = when (simState) {
                TelephonyManager.SIM_STATE_UNKNOWN -> "UNKNOWN"
                TelephonyManager.SIM_STATE_ABSENT -> "ABSENT ❌ (No SIM card)"
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN_REQUIRED"
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK_REQUIRED"
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "NETWORK_LOCKED"
                TelephonyManager.SIM_STATE_READY -> "READY ✅"
                else -> "UNKNOWN ($simState)"
            }
            Log.d("SmsDiagnostics", "3. SIM state: $simStateLabel")

            // 4. Check network connectivity
            val networkTypeLabel = try {
                val networkType = telephonyManager.networkType
                when (networkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                    TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                    TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
                    TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0"
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A"
                    TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
                    TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
                    TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
                    TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                    TelephonyManager.NETWORK_TYPE_IDEN -> "IDEN"
                    TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO_B"
                    TelephonyManager.NETWORK_TYPE_LTE -> "LTE ✅"
                    TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
                    TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPAP"
                    else -> "UNKNOWN ($networkType)"
                }
            } catch (e: SecurityException) {
                "RESTRICTED (permission denied)"
            } catch (e: Exception) {
                "ERROR (${e.message})"
            }
            Log.d("SmsDiagnostics", "4. Network type: $networkTypeLabel")

            // 5. Check operator
            val (operatorName, operatorCode) = try {
                val name = telephonyManager.simOperatorName ?: "Unknown"
                val code = telephonyManager.simOperator ?: "Unknown"
                name to code
            } catch (e: Exception) {
                "Unknown" to "Unknown"
            }
            Log.d("SmsDiagnostics", "5. Carrier: $operatorName ($operatorCode)")

            // 6. Check country
            val countryIso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                telephonyManager.networkCountryIso ?: "Unknown"
            } else {
                telephonyManager.simCountryIso ?: "Unknown"
            }
            Log.d("SmsDiagnostics", "6. Country: $countryIso")
        }

        // 7. Check if SmsManager is available
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(android.telephony.SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            android.telephony.SmsManager.getDefault()
        }
        Log.d("SmsDiagnostics", "7. SmsManager available: ${smsManager != null}")

        Log.d("SmsDiagnostics", "═══════════════════════════")
        Log.d("SmsDiagnostics", "RECOMMENDATIONS:")
        Log.d("SmsDiagnostics", "- Ensure device has an active SIM card")
        Log.d("SmsDiagnostics", "- Ensure mobile service is enabled (not in airplane mode)")
        Log.d("SmsDiagnostics", "- Ensure app has SEND_SMS permission granted")
        Log.d("SmsDiagnostics", "- For real testing, use a physical device (emulator SMS is unreliable)")
    }
}

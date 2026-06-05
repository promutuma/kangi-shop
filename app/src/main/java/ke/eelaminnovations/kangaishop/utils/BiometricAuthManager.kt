package ke.eelaminnovations.kangaishop.utils

import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BiometricAuthManager(private val activity: ComponentActivity) {

    suspend fun authenticate(): BiometricResult = suspendCancellableCoroutine { continuation ->
        val biometricManager = BiometricManager.from(activity)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            continuation.resume(BiometricResult.NotAvailable)
            return@suspendCancellableCoroutine
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                continuation.resume(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                continuation.resume(BiometricResult.Error(errString.toString()))
            }

            override fun onAuthenticationFailed() {
                continuation.resume(BiometricResult.Failed)
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Kangai Shop")
            .setSubtitle("Verify your fingerprint or face")
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    sealed class BiometricResult {
        object Success : BiometricResult()
        object Failed : BiometricResult()
        object NotAvailable : BiometricResult()
        data class Error(val message: String) : BiometricResult()
    }
}

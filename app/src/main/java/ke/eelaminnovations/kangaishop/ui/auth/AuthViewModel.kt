package ke.eelaminnovations.kangaishop.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object CodeSent : AuthUiState
    object Verifying : AuthUiState
    object SetupRequired : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val settings: AppSettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun checkCurrentUser() {
        val user = auth.currentUser
        if (user != null) {
            checkShopMapping(user.uid)
        }
    }

    fun startPhoneVerification(activity: Activity, phone: String) {
        _uiState.value = AuthUiState.Verifying
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Verification failed.")
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@AuthViewModel.verificationId = verificationId
                    this@AuthViewModel.resendToken = token
                    _uiState.value = AuthUiState.CodeSent
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(code: String) {
        val id = verificationId ?: return
        _uiState.value = AuthUiState.Verifying
        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithCredential(credential).await()
                val uid = result.user?.uid
                if (uid != null) {
                    checkShopMapping(uid)
                } else {
                    _uiState.value = AuthUiState.Error("Failed to obtain user profile.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Authentication failed.")
            }
        }
    }

    private fun checkShopMapping(uid: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val shopId = doc.getString("shopId") ?: ""
                    val role = doc.getString("role") ?: "ATTENDANT"
                    val phone = doc.getString("phone") ?: ""
                    
                    settings.setShopId(shopId)
                    settings.setSetupComplete(true)
                    settings.setAppUserId(uid)
                    
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.SetupRequired
                }
            } catch (e: Exception) {
                // Offline or Firestore rules prevent check, fallback to setup requirements
                _uiState.value = AuthUiState.SetupRequired
            }
        }
    }
}

package ke.eelaminnovations.kangaishop.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.AppUser
import ke.eelaminnovations.kangaishop.domain.repository.AppUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val users: List<AppUser> = emptyList(),
    val selectedUser: AppUser? = null,
    val pin: String = "",
    val pinError: Boolean = false,
    val isLoading: Boolean = true,
    val loginSuccess: Boolean = false,
    val needsSetup: Boolean = false,
    val showBiometric: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: AppUserRepository,
    private val settings: AppSettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        viewModelScope.launch {
            val setupComplete = settings.setupComplete.first()
            if (!setupComplete) {
                _uiState.update { it.copy(isLoading = false, needsSetup = true) }
                return@launch
            }
            signInToFirebaseAnonymously()
            userRepository.getAllUsers().collect { users ->
                _uiState.update { it.copy(users = users, isLoading = false) }
            }
        }
    }

    private suspend fun signInToFirebaseAnonymously() {
        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
        } catch (e: Exception) {
            // Firebase not configured or offline — app continues in offline mode
        }
    }

    fun selectUser(user: AppUser) {
        _uiState.update { it.copy(selectedUser = user, pin = "", pinError = false, showBiometric = true) }
    }

    fun onPinDigit(digit: String) {
        val currentPin = _uiState.value.pin
        if (currentPin.length < 4) {
            val newPin = currentPin + digit
            _uiState.update { it.copy(pin = newPin, pinError = false) }
            if (newPin.length == 4) verifyPin(newPin)
        }
    }

    fun onPinDelete() {
        val currentPin = _uiState.value.pin
        if (currentPin.isNotEmpty()) {
            _uiState.update { it.copy(pin = currentPin.dropLast(1), pinError = false) }
        }
    }

    private fun verifyPin(pin: String) {
        val user = _uiState.value.selectedUser ?: return
        viewModelScope.launch {
            val valid = userRepository.verifyPin(user.id, pin)
            if (valid) {
                settings.setAppUserId(user.id)
                _uiState.update { it.copy(loginSuccess = true) }
            } else {
                _uiState.update { it.copy(pinError = true, pin = "") }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedUser = null, pin = "", pinError = false, showBiometric = false) }
    }

    fun onBiometricSuccess() {
        val user = _uiState.value.selectedUser ?: return
        viewModelScope.launch {
            settings.setAppUserId(user.id)
            _uiState.update { it.copy(loginSuccess = true, showBiometric = false) }
        }
    }

    fun onBiometricFailed() {
        _uiState.update { it.copy(showBiometric = false) }
    }
}

package ke.eelaminnovations.kangaishop.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.AppUser
import ke.eelaminnovations.kangaishop.domain.model.UserRole
import ke.eelaminnovations.kangaishop.domain.repository.AppUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SetupUiState(
    val shopName: String = "",
    val ownerName: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val step: SetupStep = SetupStep.SHOP_INFO,
    val pinMismatch: Boolean = false,
    val isSaving: Boolean = false,
    val setupComplete: Boolean = false
)

enum class SetupStep { SHOP_INFO, CREATE_PIN }

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userRepository: AppUserRepository,
    private val settings: AppSettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState

    fun setShopName(name: String) = _uiState.update { it.copy(shopName = name) }
    fun setOwnerName(name: String) = _uiState.update { it.copy(ownerName = name) }
    fun setPin(pin: String) = _uiState.update { it.copy(pin = pin, pinMismatch = false) }
    fun setConfirmPin(pin: String) = _uiState.update { it.copy(confirmPin = pin, pinMismatch = false) }

    fun nextStep() {
        _uiState.update { it.copy(step = SetupStep.CREATE_PIN) }
    }

    fun onPinDigit(digit: String) {
        val state = _uiState.value
        if (state.pin.length < 4) {
            _uiState.update { it.copy(pin = it.pin + digit, pinMismatch = false) }
        } else if (state.confirmPin.length < 4) {
            val newConfirm = state.confirmPin + digit
            _uiState.update { it.copy(confirmPin = newConfirm, pinMismatch = false) }
            if (newConfirm.length == 4) attemptFinish(state.pin, newConfirm)
        }
    }

    fun onPinDelete() {
        val state = _uiState.value
        when {
            state.confirmPin.isNotEmpty() -> _uiState.update { it.copy(confirmPin = it.confirmPin.dropLast(1)) }
            state.pin.isNotEmpty() -> _uiState.update { it.copy(pin = it.pin.dropLast(1)) }
        }
    }

    private fun attemptFinish(pin: String, confirmPin: String) {
        if (pin != confirmPin) {
            _uiState.update { it.copy(pinMismatch = true, confirmPin = "", pin = "") }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val state = _uiState.value
            val ownerId = UUID.randomUUID().toString()
            val owner = AppUser(
                id = ownerId,
                name = state.ownerName.ifBlank { "Owner" },
                phone = "",
                pin = pin,
                role = UserRole.OWNER,
                isActive = true
            )
            userRepository.insertUser(owner)
            settings.setShopName(state.shopName.ifBlank { "Kangai Shop" })
            settings.setAppUserId(ownerId)
            settings.setSetupComplete(true)
            _uiState.update { it.copy(isSaving = false, setupComplete = true) }
        }
    }
}

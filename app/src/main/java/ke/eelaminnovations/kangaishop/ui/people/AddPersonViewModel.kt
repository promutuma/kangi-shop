package ke.eelaminnovations.kangaishop.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.domain.model.Person
import ke.eelaminnovations.kangaishop.domain.model.PersonRole
import ke.eelaminnovations.kangaishop.domain.repository.PersonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddPersonUiState(
    val name: String = "",
    val phone: String = "",
    val role: PersonRole = PersonRole.CONTACT_ONLY,
    val smsEnabled: Boolean = true,
    val notes: String = "",
    val duplicatePerson: Person? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val existingPerson: Person? = null
)

@HiltViewModel
class AddPersonViewModel @Inject constructor(
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPersonUiState())
    val uiState: StateFlow<AddPersonUiState> = _uiState

    fun loadPerson(personId: String) {
        viewModelScope.launch {
            val person = personRepository.getPersonById(personId) ?: return@launch
            _uiState.update {
                it.copy(
                    name = person.name,
                    phone = person.phone,
                    role = person.role,
                    smsEnabled = person.smsEnabled,
                    notes = person.notes,
                    existingPerson = person
                )
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name) }
    fun setPhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
        if (phone.length >= 10) checkDuplicate(phone)
    }
    fun setSmsEnabled(enabled: Boolean) = _uiState.update { it.copy(smsEnabled = enabled) }
    fun setNotes(notes: String) = _uiState.update { it.copy(notes = notes) }

    private fun checkDuplicate(phone: String) {
        viewModelScope.launch {
            val existing = personRepository.getPersonByPhone(phone)
            val currentId = _uiState.value.existingPerson?.id
            val duplicate = if (existing?.id != currentId) existing else null
            _uiState.update { it.copy(duplicatePerson = duplicate) }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank() || state.phone.isBlank()) return
        if (state.duplicatePerson != null) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val existing = state.existingPerson
            if (existing != null) {
                personRepository.updatePerson(existing.copy(name = state.name, phone = state.phone, role = state.role, smsEnabled = state.smsEnabled, notes = state.notes))
            } else {
                personRepository.insertPerson(Person(
                    id = UUID.randomUUID().toString(),
                    name = state.name,
                    phone = state.phone,
                    role = state.role,
                    smsEnabled = state.smsEnabled,
                    notes = state.notes
                ))
            }
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun setRole(role: PersonRole) = _uiState.update { it.copy(role = role) }
}

package ke.eelaminnovations.kangaishop.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PeopleFilter { ALL, SUPPLIERS, CUSTOMERS }

data class PeopleUiState(
    val people: List<PersonWithRole> = emptyList(),
    val filter: PeopleFilter = PeopleFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val currentUserRole: UserRole = UserRole.ATTENDANT
)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val userRepository: AppUserRepository,
    private val settings: AppSettingsDataStore
) : ViewModel() {

    private val _filter = MutableStateFlow(PeopleFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<PeopleUiState> = combine(
        combine(
            _searchQuery.flatMapLatest { q ->
                if (q.isEmpty()) personRepository.getAllPeople()
                else personRepository.searchPeople(q)
            },
            ledgerTransactionRepository.getSupplierDebts(),
            ledgerTransactionRepository.getCustomerDebts()
        ) { a, b, c -> Triple(a, b, c) },
        _filter,
        _searchQuery,
        combine(settings.appUserId, userRepository.getAllUsers()) { id, list -> list.find { it.id == id } }
    ) { triple, filter, query, user ->
        val people = triple.first
        val supplierDebts = triple.second
        val customerDebts = triple.third
        val userRole = user?.role ?: UserRole.ATTENDANT

        val withRoles = people.map { person ->
            val role = person.role
            val supplierBalance = supplierDebts[person.id] ?: 0.0
            val customerBalance = customerDebts[person.id] ?: 0.0

            val netBalance = when (role) {
                PersonRole.SUPPLIER -> supplierBalance
                PersonRole.CUSTOMER -> customerBalance
                PersonRole.BOTH -> supplierBalance - customerBalance
                PersonRole.CONTACT_ONLY -> 0.0
            }
            PersonWithRole(person, role, netBalance)
        }
        val filtered = when (filter) {
            PeopleFilter.ALL -> withRoles
            PeopleFilter.SUPPLIERS -> withRoles.filter { it.role == PersonRole.SUPPLIER || it.role == PersonRole.BOTH }
            PeopleFilter.CUSTOMERS -> withRoles.filter { it.role == PersonRole.CUSTOMER || it.role == PersonRole.BOTH }
        }
        PeopleUiState(people = filtered, filter = filter, searchQuery = query, isLoading = false, currentUserRole = userRole)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PeopleUiState()
    )


    fun setFilter(filter: PeopleFilter) { _filter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
}

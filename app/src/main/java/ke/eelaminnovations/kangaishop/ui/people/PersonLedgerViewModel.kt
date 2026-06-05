package ke.eelaminnovations.kangaishop.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.domain.repository.*
import ke.eelaminnovations.kangaishop.domain.usecase.*
import ke.eelaminnovations.kangaishop.utils.startOfMonth
import ke.eelaminnovations.kangaishop.utils.endOfMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LedgerFilter { THIS_MONTH, ALL_TIME }

data class PersonLedgerUiState(
    val person: Person? = null,
    val role: PersonRole = PersonRole.CONTACT_ONLY,
    val netBalance: Double = 0.0,
    val transactions: List<LedgerTransaction> = emptyList(),
    val filter: LedgerFilter = LedgerFilter.THIS_MONTH,
    val isLoading: Boolean = true,
    val snackMessage: String? = null,
    val currentUserRole: UserRole = UserRole.ATTENDANT,
    val creditLimit: Double = 2000.0
)

private data class LedgerData(
    val transactions: List<LedgerTransaction>,
    val person: Person?,
    val supplierDebts: Map<String, Double>,
    val customerDebts: Map<String, Double>,
    val filter: LedgerFilter
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PersonLedgerViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val milkDeliveryRepository: MilkDeliveryRepository,
    private val recordPaymentUseCase: RecordPaymentUseCase,
    private val recordCreditUseCase: RecordCreditUseCase,
    private val recordGoodsUseCase: RecordGoodsUseCase,
    private val userRepository: AppUserRepository,
    private val settings: AppSettingsDataStore
) : ViewModel() {

    private val _personId = MutableStateFlow<String?>(null)
    private val _filter = MutableStateFlow(LedgerFilter.THIS_MONTH)
    private val _snackMessage = MutableStateFlow<String?>(null)
    private val _lastSavedTxId = MutableStateFlow<String?>(null)
    private val _snackIsUndoable = MutableStateFlow(false)

    private val ledgerData: Flow<LedgerData> = combine(
        _personId, _filter
    ) { id, filter -> id to filter }.flatMapLatest { (id, filter) ->
        if (id == null) return@flatMapLatest flowOf(
            LedgerData(emptyList(), null, emptyMap(), emptyMap(), filter)
        )
        val now = System.currentTimeMillis()
        val txFlow = if (filter == LedgerFilter.THIS_MONTH) {
            ledgerTransactionRepository.getTransactionsForPersonInRange(id, startOfMonth(now), endOfMonth(now))
        } else {
            ledgerTransactionRepository.getTransactionsForPerson(id)
        }
        combine(
            txFlow,
            personRepository.getAllPeople().map { list -> list.find { it.id == id } },
            ledgerTransactionRepository.getSupplierDebts(),
            ledgerTransactionRepository.getCustomerDebts()
        ) { txs, person, supplierDebts, customerDebts ->
            LedgerData(txs, person, supplierDebts, customerDebts, filter)
        }
    }

    val snackIsUndoable: StateFlow<Boolean> = _snackIsUndoable

    val uiState: StateFlow<PersonLedgerUiState> = combine(
        ledgerData,
        _snackMessage,
        combine(settings.appUserId, userRepository.getAllUsers()) { id, list -> list.find { it.id == id } },
        settings.creditLimit
    ) { data, snack, user, limit ->
        val id = _personId.value
        val role = data.person?.role ?: PersonRole.CONTACT_ONLY
        val supplierBalance = data.supplierDebts[id] ?: 0.0
        val customerBalance = data.customerDebts[id] ?: 0.0

        val netBalance = when (role) {
            PersonRole.SUPPLIER -> supplierBalance
            PersonRole.CUSTOMER -> customerBalance
            PersonRole.BOTH -> supplierBalance - customerBalance
            PersonRole.CONTACT_ONLY -> 0.0
        }
        val userRole = user?.role ?: UserRole.ATTENDANT

        PersonLedgerUiState(
            person = data.person,
            role = role,
            netBalance = netBalance,
            transactions = data.transactions,
            filter = data.filter,
            isLoading = false,
            snackMessage = snack,
            currentUserRole = userRole,
            creditLimit = limit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PersonLedgerUiState()
    )


    fun loadPerson(personId: String) { _personId.value = personId }
    fun setFilter(filter: LedgerFilter) { _filter.value = filter }
    fun clearSnack() {
        _snackMessage.value = null
        _snackIsUndoable.value = false
    }

    fun recordPayment(
        type: TransactionType,
        amount: Double,
        mpesaRef: String? = null,
        goodsDescription: String? = null,
        notes: String? = null
    ) {
        val personId = _personId.value ?: return
        viewModelScope.launch {
            val tx = recordPaymentUseCase(personId, type, amount, mpesaRef, goodsDescription, notes)
            _lastSavedTxId.value = tx.id
            _snackIsUndoable.value = true
            _snackMessage.value = "✅ Payment saved."
        }
    }

    fun recordCredit(description: String, amount: Double, notes: String? = null) {
        val personId = _personId.value ?: return
        viewModelScope.launch {
            val tx = recordCreditUseCase(personId, description, amount, notes)
            _lastSavedTxId.value = tx.id
            _snackIsUndoable.value = true
            _snackMessage.value = "✅ Credit saved."
        }
    }

    fun recordGoods(description: String, amount: Double, notes: String? = null) {
        val personId = _personId.value ?: return
        viewModelScope.launch {
            val tx = recordGoodsUseCase(personId, description, amount, notes)
            _lastSavedTxId.value = tx.id
            _snackIsUndoable.value = true
            _snackMessage.value = "✅ Goods purchase recorded."
        }
    }

    fun undoLastTransaction() {
        val txId = _lastSavedTxId.value ?: return
        viewModelScope.launch {
            ledgerTransactionRepository.deleteTransaction(txId)
            _lastSavedTxId.value = null
            _snackIsUndoable.value = false
            _snackMessage.value = null
        }
    }

    fun deletePerson(onComplete: () -> Unit) {
        val personId = _personId.value ?: return
        viewModelScope.launch {
            personRepository.deletePerson(personId)
            onComplete()
        }
    }

    fun deleteTransaction(transaction: LedgerTransaction) {
        viewModelScope.launch {
            if (transaction.type == TransactionType.MILK_DELIVERY) {
                transaction.milkDeliveryId?.let { milkDeliveryRepository.deleteDelivery(it) }
            }
            ledgerTransactionRepository.deleteTransaction(transaction.id)
            _snackMessage.value = "🗑 Transaction deleted."
        }
    }
}

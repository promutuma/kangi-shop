package ke.eelaminnovations.kangaishop.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.domain.repository.*
import ke.eelaminnovations.kangaishop.utils.todayStartEpoch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import java.util.Calendar
import javax.inject.Inject

data class HomeSummaryData(
    val morningLitres: Double = 0.0,
    val eveningLitres: Double = 0.0,
    val eveningRecorded: Boolean = false,
    val totalOwedOut: Double = 0.0,
    val totalOwedIn: Double = 0.0,
    val highBalanceAlerts: List<Pair<Person, Double>> = emptyList(),
    val overdueCustomers: List<Pair<Person, Long>> = emptyList(),
    val eveningWarning: Boolean = false,
    val currentUserName: String = "",
    val currentUserRole: UserRole = UserRole.ATTENDANT
)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeSummaryData) : HomeUiState
}

// Keep old alias for code that still references HomeSummary
typealias HomeSummary = HomeSummaryData

private data class HomeRawData(
    val deliveries: List<MilkDelivery>,
    val supplierDebts: Map<String, Double>,
    val customerDebts: Map<String, Double>,
    val people: List<Person>,
    val user: AppUser?,
    val debtThreshold: Double,
    val overdueDays: Int,
    val lastPaymentDates: Map<String, Long>,
    val lastCreditDates: Map<String, Long>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val milkDeliveryRepository: MilkDeliveryRepository,
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val personRepository: PersonRepository,
    private val userRepository: AppUserRepository,
    private val settings: AppSettingsDataStore
) : ViewModel() {

    private val clockTick: Flow<Unit> = flow {
        while (currentCoroutineContext().isActive) {
            emit(Unit)
            delay(5 * 60 * 1000L)
        }
    }

    private val rawData: Flow<HomeRawData> = combine(
        milkDeliveryRepository.getDeliveriesForDate(todayStartEpoch()),
        ledgerTransactionRepository.getSupplierDebts(),
        ledgerTransactionRepository.getCustomerDebts(),
        personRepository.getAllPeople(),
        ledgerTransactionRepository.getLastPaymentDates(),
        ledgerTransactionRepository.getLastCreditDates()
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        val d = array[0] as List<MilkDelivery>
        @Suppress("UNCHECKED_CAST")
        val s = array[1] as Map<String, Double>
        @Suppress("UNCHECKED_CAST")
        val c = array[2] as Map<String, Double>
        @Suppress("UNCHECKED_CAST")
        val p = array[3] as List<Person>
        @Suppress("UNCHECKED_CAST")
        val lpay = array[4] as Map<String, Long>
        @Suppress("UNCHECKED_CAST")
        val lcred = array[5] as Map<String, Long>
        HomeRawData(
            deliveries = d,
            supplierDebts = s,
            customerDebts = c,
            people = p,
            user = null,
            debtThreshold = 5000.0,
            overdueDays = 7,
            lastPaymentDates = lpay,
            lastCreditDates = lcred
        )
    }
        .combine(combine(settings.appUserId, userRepository.getAllUsers()) { id, list -> list.find { it.id == id } }) { data, user ->
            data.copy(user = user)
        }
        .combine(combine(settings.debtAlertThreshold, settings.customerOverdueDays) { t, d -> t to d }) { data, (t, d) ->
            data.copy(debtThreshold = t, overdueDays = d)
        }

    val uiState: StateFlow<HomeUiState> = combine(rawData, clockTick) { data, _ -> data }
        .map { data ->
            val morningLitres = data.deliveries.filter { it.session == MilkSession.MORNING }.sumOf { it.litres }
            val eveningLitres = data.deliveries.filter { it.session == MilkSession.EVENING }.sumOf { it.litres }
            val eveningRecorded = data.deliveries.any { it.session == MilkSession.EVENING }
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            HomeUiState.Success(HomeSummaryData(
                morningLitres = morningLitres,
                eveningLitres = eveningLitres,
                eveningRecorded = eveningRecorded,
                totalOwedOut = data.supplierDebts.values.filter { it > 0 }.sum(),
                totalOwedIn = data.customerDebts.values.filter { it > 0 }.sum(),
                highBalanceAlerts = data.supplierDebts.entries
                    .filter { it.value > data.debtThreshold }
                    .mapNotNull { (id, bal) -> data.people.find { it.id == id }?.let { it to bal } }
                    .sortedByDescending { it.second },
                overdueCustomers = data.customerDebts.entries
                    .filter { it.value > 0.01 }
                    .mapNotNull { (id, _) -> data.people.find { it.id == id } }
                    .filter { person ->
                        val lastPayment = data.lastPaymentDates[person.id]
                        val lastCredit = data.lastCreditDates[person.id]
                        val refDate = lastPayment ?: lastCredit ?: 0L
                        val diffMs = System.currentTimeMillis() - refDate
                        val days = diffMs / (24 * 3600 * 1000L)
                        days >= data.overdueDays
                    }
                    .map { person ->
                        val lastPayment = data.lastPaymentDates[person.id]
                        val lastCredit = data.lastCreditDates[person.id]
                        person to (lastPayment ?: lastCredit ?: 0L)
                    },
                eveningWarning = !eveningRecorded && hour >= 17,
                currentUserName = data.user?.name ?: "User",
                currentUserRole = data.user?.role ?: UserRole.ATTENDANT
            ))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}

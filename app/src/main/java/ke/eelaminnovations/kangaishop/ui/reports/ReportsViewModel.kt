package ke.eelaminnovations.kangaishop.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.domain.repository.*
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

enum class ReportPeriod { TODAY, THIS_WEEK, THIS_MONTH }

data class DailyMilkAggregate(
    val dayStart: Long,
    val dayLabel: String,
    val morningLitres: Double,
    val eveningLitres: Double
)

data class MonthlyMilkAggregate(
    val monthStart: Long,
    val monthLabel: String,
    val totalLitres: Double
)

data class ReportSummary(
    val period: ReportPeriod = ReportPeriod.THIS_MONTH,
    val totalMilkLitres: Double = 0.0,
    val totalMilkValue: Double = 0.0,
    val morningLitres: Double = 0.0,
    val eveningLitres: Double = 0.0,
    val supplierBalances: List<Pair<Person, Double>> = emptyList(),
    val customerBalances: List<Pair<Person, Double>> = emptyList(),
    val totalOwedOut: Double = 0.0,
    val totalOwedIn: Double = 0.0,
    val dailyMilk: List<DailyMilkAggregate> = emptyList(),
    val monthlyMilk: List<MonthlyMilkAggregate> = emptyList(),
    val isLoading: Boolean = true,
    val currentUserRole: UserRole = UserRole.ATTENDANT
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val milkDeliveryRepository: MilkDeliveryRepository,
    private val personRepository: PersonRepository,
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val userRepository: AppUserRepository,
    private val settings: AppSettingsDataStore
) : ViewModel() {

    private val _period = MutableStateFlow(ReportPeriod.THIS_MONTH)

    private val userData = combine(settings.appUserId, userRepository.getAllUsers()) { id, list -> list.find { it.id == id } }

    val uiState: StateFlow<ReportSummary> = combine(
        combine(
            _period.flatMapLatest { _ ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -5)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start6MonthsAgo = cal.timeInMillis
                val endToday = System.currentTimeMillis() + 86_400_000L
                milkDeliveryRepository.getAllDeliveriesInRange(start6MonthsAgo, endToday)
            },
            personRepository.getAllPeople(),
            ledgerTransactionRepository.getSupplierDebts(),
            ledgerTransactionRepository.getCustomerDebts()
        ) { a, b, c, d -> listOf(a, b, c, d) },
        userData,
        _period
    ) { blocks, user, period ->
        @Suppress("UNCHECKED_CAST")
        val deliveries = blocks[0] as List<MilkDelivery>
        @Suppress("UNCHECKED_CAST")
        val people = blocks[1] as List<Person>
        @Suppress("UNCHECKED_CAST")
        val supplierDebts = blocks[2] as Map<String, Double>
        @Suppress("UNCHECKED_CAST")
        val customerDebts = blocks[3] as Map<String, Double>

        val currentRange = periodRange(period)
        val currentPeriodDeliveries = deliveries.filter { it.deliveryDate >= currentRange.first && it.deliveryDate < currentRange.second }

        val totalLitres = currentPeriodDeliveries.sumOf { it.litres }
        val totalValue = currentPeriodDeliveries.sumOf { it.totalValue }
        val morningLitres = currentPeriodDeliveries.filter { it.session == MilkSession.MORNING }.sumOf { it.litres }
        val eveningLitres = currentPeriodDeliveries.filter { it.session == MilkSession.EVENING }.sumOf { it.litres }

        val supplierPairs = supplierDebts.entries
            .mapNotNull { (id, balance) -> people.find { it.id == id }?.let { it to balance } }
            .sortedByDescending { it.second }
        val customerPairs = customerDebts.entries
            .mapNotNull { (id, balance) -> people.find { it.id == id }?.let { it to balance } }
            .sortedByDescending { it.second }

        // Compute Daily Milk Aggregates for active period
        val dailyMap = currentPeriodDeliveries.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.deliveryDate }
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        val dailyAggregates = dailyMap.map { (dayStart, list) ->
            val label = when (period) {
                ReportPeriod.TODAY -> "Today"
                ReportPeriod.THIS_WEEK -> java.text.SimpleDateFormat("EEE", java.util.Locale.US).format(java.util.Date(dayStart))
                ReportPeriod.THIS_MONTH -> java.text.SimpleDateFormat("d", java.util.Locale.US).format(java.util.Date(dayStart))
            }
            DailyMilkAggregate(
                dayStart = dayStart,
                dayLabel = label,
                morningLitres = list.filter { it.session == MilkSession.MORNING }.sumOf { it.litres },
                eveningLitres = list.filter { it.session == MilkSession.EVENING }.sumOf { it.litres }
            )
        }.sortedBy { it.dayStart }

        // Compute Monthly Milk Aggregates for trend line over the past 6 months
        val monthlyMap = deliveries.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.deliveryDate }
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        val monthlyAggregates = monthlyMap.map { (monthStart, list) ->
            val label = java.text.SimpleDateFormat("MMM", java.util.Locale.US).format(java.util.Date(monthStart))
            MonthlyMilkAggregate(
                monthStart = monthStart,
                monthLabel = label,
                totalLitres = list.sumOf { it.litres }
            )
        }.sortedBy { it.monthStart }

        val role = user?.role ?: UserRole.ATTENDANT

        ReportSummary(
            period = period,
            totalMilkLitres = totalLitres,
            totalMilkValue = totalValue,
            morningLitres = morningLitres,
            eveningLitres = eveningLitres,
            supplierBalances = supplierPairs,
            customerBalances = customerPairs,
            totalOwedOut = supplierDebts.values.filter { it > 0 }.sum(),
            totalOwedIn = customerDebts.values.filter { it > 0 }.sum(),
            dailyMilk = dailyAggregates,
            monthlyMilk = monthlyAggregates,
            isLoading = false,
            currentUserRole = role
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportSummary()
    )

    fun setPeriod(period: ReportPeriod) { _period.value = period }

    fun getDeliveriesForDay(dayStart: Long): Flow<List<Pair<MilkDelivery, Person>>> {
        return combine(
            milkDeliveryRepository.getAllDeliveriesInRange(dayStart, dayStart + 86_400_000L),
            personRepository.getAllPeople()
        ) { deliveries, people ->
            deliveries.mapNotNull { delivery ->
                val person = people.find { it.id == delivery.personId }
                if (person != null) delivery to person else null
            }
        }
    }

    private fun periodRange(period: ReportPeriod): Pair<Long, Long> {

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        return when (period) {
            ReportPeriod.TODAY -> {
                val start = todayStartEpoch()
                start to (start + 86_400_000L)
            }
            ReportPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                start to cal.timeInMillis
            }
            ReportPeriod.THIS_MONTH -> startOfMonth(now) to endOfMonth(now)
        }
    }
}

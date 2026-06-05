package ke.eelaminnovations.kangaishop.ui.milk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.domain.repository.*
import ke.eelaminnovations.kangaishop.domain.usecase.RecordDeliveryUseCase
import ke.eelaminnovations.kangaishop.utils.todayStartEpoch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MilkSuccessData(
    val dailySummaries: List<DailySupplierSummary> = emptyList(),
    val people: List<Person> = emptyList(),
    val morningDefaultPrice: Double = 65.0,
    val eveningDefaultPrice: Double = 60.0,
    val undoDeliveryId: String? = null,
    val undoMessage: String? = null
)

sealed interface MilkUiState {
    data object Loading : MilkUiState
    data class Success(val data: MilkSuccessData) : MilkUiState
}

@HiltViewModel
class MilkViewModel @Inject constructor(
    private val milkDeliveryRepository: MilkDeliveryRepository,
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val personRepository: PersonRepository,
    private val recordDeliveryUseCase: RecordDeliveryUseCase,
    private val settings: AppSettingsDataStore
) : ViewModel() {

    private val _undoDeliveryId = MutableStateFlow<String?>(null)
    private val _undoMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MilkUiState> = combine(
        milkDeliveryRepository.getDeliveriesForDate(todayStartEpoch()),
        personRepository.getAllPeople(),
        settings.morningPrice,
        settings.eveningPrice,
        ledgerTransactionRepository.getSupplierDebts()
    ) { deliveries, people, morningPrice, eveningPrice, supplierDebts ->
        MilkUiState.Success(MilkSuccessData(
            dailySummaries = people
                .filter { person -> deliveries.any { it.personId == person.id } }
                .map { person ->
                    val balance = when (person.role) {
                        PersonRole.SUPPLIER, PersonRole.BOTH -> supplierDebts[person.id] ?: 0.0
                        else -> 0.0
                    }
                    DailySupplierSummary(
                        person = person,
                        morningDelivery = deliveries.find { it.personId == person.id && it.session == MilkSession.MORNING },
                        eveningDelivery = deliveries.find { it.personId == person.id && it.session == MilkSession.EVENING },
                        netBalance = balance
                    )
                },
            people = people,
            morningDefaultPrice = morningPrice,
            eveningDefaultPrice = eveningPrice,
            undoDeliveryId = _undoDeliveryId.value,
            undoMessage = _undoMessage.value
        ))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MilkUiState.Loading)

    fun searchPeople(query: String): Flow<List<Person>> = personRepository.searchPeople(query)

    fun recordDelivery(
        person: Person,
        isMorning: Boolean,
        litres: Double,
        pricePerLitre: Double,
        quality: MilkQuality = MilkQuality.GOOD,
        rejectedLitres: Double = 0.0,
        notes: String = "",
        deliveryDate: Long = System.currentTimeMillis(),
        onSaved: (deliveryId: String, message: String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val delivery = MilkDelivery(
                id = UUID.randomUUID().toString(),
                personId = person.id,
                deliveryDate = deliveryDate,
                session = if (isMorning) MilkSession.MORNING else MilkSession.EVENING,
                litres = litres,
                pricePerLitre = pricePerLitre,
                totalValue = litres * pricePerLitre,
                quality = quality,
                rejectedLitres = rejectedLitres,
                notes = notes
            )
            recordDeliveryUseCase(delivery)
            val sessionLabel = if (isMorning) "Morning" else "Evening"
            val msg = "$sessionLabel milk saved — ${person.name}: ${litres}L · KES ${litres * pricePerLitre}"
            _undoDeliveryId.value = delivery.id
            _undoMessage.value = msg
            onSaved(delivery.id, msg)
        }
    }

    fun undoLastDelivery() {
        val id = _undoDeliveryId.value ?: return
        viewModelScope.launch {
            // Mark delivery as deleted (soft-delete) instead of removing
            milkDeliveryRepository.deleteDelivery(id)

            // Find the original ledger transaction and mark it as deleted
            // Don't physically delete — keep for audit trail
            ledgerTransactionRepository.getTransactionsByType("", listOf(TransactionType.MILK_DELIVERY))
                .first().find { it.milkDeliveryId == id }?.let { original ->
                    // Create a compensating transaction (reversal) with parentTransactionId link
                    val reversal = original.copy(
                        id = UUID.randomUUID().toString(),
                        amount = -original.amount,  // Negate to reverse
                        direction = when (original.direction) {
                            TransactionDirection.DEBIT -> TransactionDirection.CREDIT
                            TransactionDirection.CREDIT -> TransactionDirection.DEBIT
                        },
                        parentTransactionId = original.id,  // Link to original
                        notes = "${original.notes ?: ""}\n[REVERSED by attendant]",
                        transactionDate = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        lastModifiedAt = System.currentTimeMillis()
                    )
                    ledgerTransactionRepository.insertTransaction(reversal)
                }
            _undoDeliveryId.value = null
            _undoMessage.value = null
        }
    }

    fun clearUndo() {
        _undoDeliveryId.value = null
        _undoMessage.value = null
    }
}

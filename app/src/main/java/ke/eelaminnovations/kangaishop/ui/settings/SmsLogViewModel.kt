package ke.eelaminnovations.kangaishop.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.eelaminnovations.kangaishop.domain.model.SmsLog
import ke.eelaminnovations.kangaishop.domain.model.SmsStatus
import ke.eelaminnovations.kangaishop.domain.repository.SmsLogRepository
import ke.eelaminnovations.kangaishop.utils.SmsHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsLogViewModel @Inject constructor(
    private val smsLogRepository: SmsLogRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val smsLogs: StateFlow<List<SmsLog>> = smsLogRepository.getAllSmsLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun retryFailedSms(log: SmsLog) {
        viewModelScope.launch {
            val sent = SmsHelper.sendSms(context, log.recipientPhone, log.message, log.id)
            if (sent) {
                smsLogRepository.updateSmsLog(
                    log.copy(
                        status = SmsStatus.SENT,
                        errorMessage = null,
                        sentAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}

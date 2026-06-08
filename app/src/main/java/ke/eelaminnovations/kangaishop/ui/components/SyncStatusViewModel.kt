package ke.eelaminnovations.kangaishop.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.eelaminnovations.kangaishop.data.local.dao.LedgerTransactionDao
import ke.eelaminnovations.kangaishop.data.local.dao.MilkDeliveryDao
import ke.eelaminnovations.kangaishop.data.local.dao.PersonDao
import ke.eelaminnovations.kangaishop.data.workers.SyncWorker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SyncState { SYNCED, SYNCING, PENDING, OFFLINE }

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val personDao: PersonDao,
    private val milkDeliveryDao: MilkDeliveryDao,
    private val ledgerTransactionDao: LedgerTransactionDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        trySend(capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    private val isSyncing: Flow<Boolean> = flow {
        val workManager = androidx.work.WorkManager.getInstance(context)
        workManager.getWorkInfosForUniqueWorkFlow(SyncWorker.WORK_NAME).collect { workInfos ->
            val running = workInfos.any { it.state == androidx.work.WorkInfo.State.RUNNING }
            emit(running)
        }
    }

    val syncState: StateFlow<SyncState> = combine(
        personDao.getPendingCountFlow(),
        milkDeliveryDao.getPendingCountFlow(),
        ledgerTransactionDao.getPendingCountFlow(),
        isOnline,
        isSyncing
    ) { pCount, mCount, tCount, online, syncing ->
        val pendingTotal = pCount + mCount + tCount
        when {
            !online -> SyncState.OFFLINE
            syncing -> SyncState.SYNCING
            pendingTotal > 0 -> SyncState.PENDING
            else -> SyncState.SYNCED
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SyncState.SYNCED
    )
}

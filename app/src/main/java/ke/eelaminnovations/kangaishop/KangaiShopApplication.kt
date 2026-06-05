package ke.eelaminnovations.kangaishop

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.data.workers.BackupWorker
import ke.eelaminnovations.kangaishop.data.workers.SyncWorker
import ke.eelaminnovations.kangaishop.utils.SmsSentReceiver
import ke.eelaminnovations.kangaishop.utils.BuildEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltAndroidApp
class KangaiShopApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var settings: AppSettingsDataStore

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        BuildEnvironment.logEnvironment()
        registerSmsSentReceiver()
        initDeviceId()
        scheduleBackgroundWorkers()
    }

    private fun registerSmsSentReceiver() {
        val intentFilter = IntentFilter("SMS_SENT")
        ContextCompat.registerReceiver(this, SmsSentReceiver(), intentFilter, ContextCompat.RECEIVER_EXPORTED)
    }

    private fun initDeviceId() {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = settings.deviceId.first()
            if (existing.isEmpty()) {
                // Use Android ID as base, fall back to UUID if not available
                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val deviceId = if (!androidId.isNullOrBlank()) androidId else UUID.randomUUID().toString()
                settings.setDeviceId(deviceId)
            }
        }
    }

    private fun scheduleBackgroundWorkers() {
        val workManager = androidx.work.WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(SyncWorker.WORK_NAME, androidx.work.ExistingPeriodicWorkPolicy.KEEP, SyncWorker.buildRequest())
        workManager.enqueueUniquePeriodicWork(BackupWorker.WORK_NAME, androidx.work.ExistingPeriodicWorkPolicy.KEEP, BackupWorker.buildPeriodicRequest(2))
    }
}

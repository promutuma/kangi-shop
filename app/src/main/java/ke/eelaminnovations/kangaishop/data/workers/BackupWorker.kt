package ke.eelaminnovations.kangaishop.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.eelaminnovations.kangaishop.data.local.dao.*
import ke.eelaminnovations.kangaishop.data.local.entity.toEntity
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.BackupLog
import ke.eelaminnovations.kangaishop.domain.model.BackupStatus
import ke.eelaminnovations.kangaishop.utils.BackupHelper
import kotlinx.coroutines.flow.first
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.FileWriter


@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val personDao: PersonDao,
    private val milkDeliveryDao: MilkDeliveryDao,
    private val ledgerTransactionDao: LedgerTransactionDao,
    private val backupLogDao: BackupLogDao,
    private val appUserDao: AppUserDao,
    private val settings: AppSettingsDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        var tempFile: java.io.File? = null
        var fileSize = 0L
        return try {
            val account = settings.backupAccount.first()
            if (account.isEmpty()) return Result.success()

            val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
            val timestampStr = sdf.format(java.util.Date())
            val backupFileName = "kangaishop_backup_$timestampStr.json"
            
            tempFile = java.io.File(context.cacheDir, backupFileName)
            BackupHelper.exportDatabaseToFile(tempFile, personDao, milkDeliveryDao, ledgerTransactionDao, appUserDao, settings)
            fileSize = tempFile.length()

            var driveFileId: String? = null
            var errorMsg: String? = null
            var status = BackupStatus.FAILED

            try {
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf("https://www.googleapis.com/auth/drive.file")
                )
                val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
                if (googleAccount != null) {
                    credential.selectedAccount = googleAccount.account

                    val driveService = Drive.Builder(
                        com.google.api.client.http.javanet.NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        credential
                    ).setApplicationName("Kangai Shop").build()

                    val metadata = File().apply {
                        name = backupFileName
                        mimeType = "application/json"
                    }
                    val mediaContent = FileContent("application/json", tempFile)
                    val resultFile = driveService.files().create(metadata, mediaContent)
                        .setFields("id")
                        .execute()

                    driveFileId = resultFile.id
                    status = BackupStatus.SUCCESS

                    // Enforce retention: keep only the last 30 backups
                    try {
                        val resultList = driveService.files().list()
                            .setQ("name contains 'kangaishop_backup_' and mimeType = 'application/json' and trashed = false")
                            .setOrderBy("createdTime desc")
                            .setSpaces("drive")
                            .setFields("files(id, name)")
                            .execute()
                        val files = resultList.files
                        if (files != null && files.size > 30) {
                            for (i in 30 until files.size) {
                                val fileToDelete = files[i]
                                driveService.files().delete(fileToDelete.id).execute()
                            }
                        }
                    } catch (cleanupEx: Exception) {
                        cleanupEx.printStackTrace()
                    }
                } else {
                    errorMsg = "Google Account not signed in or not found."
                }
            } catch (e: Exception) {
                errorMsg = e.message ?: "Drive upload error"
                e.printStackTrace()
            }

            val log = BackupLog(
                id = UUID.randomUUID().toString(),
                backupDate = System.currentTimeMillis(),
                status = status,
                driveFileId = driveFileId,
                fileSizeBytes = fileSize,
                errorMessage = errorMsg
            )
            backupLogDao.insertBackupLog(log.toEntity())
            backupLogDao.pruneOldLogs()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        } finally {
            tempFile?.let { if (it.exists()) it.delete() }
        }
    }

    companion object {
        const val WORK_NAME = "BackupWorker"

        fun buildPeriodicRequest(hourOfDay: Int): PeriodicWorkRequest {
            val now = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance().apply {
                timeInMillis = now
                set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                if (timeInMillis <= now) add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            val initialDelay = cal.timeInMillis - now
            return PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
        }
    }
}


package ke.eelaminnovations.kangaishop.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.eelaminnovations.kangaishop.data.local.dao.*
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.data.workers.BackupWorker
import ke.eelaminnovations.kangaishop.data.workers.SyncWorker
import ke.eelaminnovations.kangaishop.domain.model.AppTheme
import ke.eelaminnovations.kangaishop.domain.model.AppUser
import ke.eelaminnovations.kangaishop.domain.model.BackupLog
import ke.eelaminnovations.kangaishop.domain.model.UserRole
import ke.eelaminnovations.kangaishop.domain.repository.AppUserRepository
import ke.eelaminnovations.kangaishop.utils.BackupHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.Drive

data class DriveBackupFile(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val createdTime: Long
)

sealed interface RestoreState {
    object Idle : RestoreState
    object Loading : RestoreState
    object Success : RestoreState
    data class Error(val message: String) : RestoreState
}

data class SettingsUiState(
    val shopName: String = "Kangai Shop",
    val shopMpesa: String = "",
    val morningPrice: Double = 65.0,
    val eveningPrice: Double = 60.0,
    val theme: AppTheme = AppTheme.DYNAMIC,
    val smsEnabled: Boolean = true,
    val debtAlertThreshold: Double = 5000.0,
    val customerOverdueDays: Int = 7,
    val backupEnabled: Boolean = true,
    val backupHour: Int = 2,
    val backupAccount: String = "",
    val creditLimit: Double = 2000.0,
    val appUsers: List<AppUser> = emptyList(),
    val backupLogs: List<BackupLog> = emptyList(),
    val currentUserRole: UserRole = UserRole.ATTENDANT,
    val darkModeOverride: Boolean? = null  // null=system, true=dark, false=light
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: AppSettingsDataStore,
    private val userRepository: AppUserRepository,
    private val personDao: PersonDao,
    private val milkDeliveryDao: MilkDeliveryDao,
    private val ledgerTransactionDao: LedgerTransactionDao,
    private val backupLogDao: BackupLogDao,
    private val appUserDao: AppUserDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _driveBackups = MutableStateFlow<List<DriveBackupFile>>(emptyList())
    val driveBackups: StateFlow<List<DriveBackupFile>> = _driveBackups.asStateFlow()

    private val _isLoadingBackups = MutableStateFlow(false)
    val isLoadingBackups: StateFlow<Boolean> = _isLoadingBackups.asStateFlow()

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(settings.shopName, settings.shopMpesa, settings.morningPrice, settings.eveningPrice) { a, b, c, d -> listOf(a, b, c, d) },
        combine(settings.theme, settings.smsEnabled, settings.debtAlertThreshold, settings.customerOverdueDays) { a, b, c, d -> listOf(a, b, c, d) },
        combine(settings.backupEnabled, settings.backupHour, settings.backupAccount, settings.creditLimit) { a, b, c, d -> listOf(a, b, c, d) },
        combine(settings.appUserId, userRepository.getAllUsers()) { id, list -> id to list },
        backupLogDao.getAllBackupLogs()
    ) { block1, block2, block3, userPair, logs ->
        val (appUserId, users) = userPair
        val currentUser = users.find { it.id == appUserId }
        val role = currentUser?.role ?: UserRole.ATTENDANT

        SettingsUiState(
            shopName = block1[0] as String,
            shopMpesa = block1[1] as String,
            morningPrice = block1[2] as Double,
            eveningPrice = block1[3] as Double,
            theme = block2[0] as AppTheme,
            smsEnabled = block2[1] as Boolean,
            debtAlertThreshold = block2[2] as Double,
            customerOverdueDays = block2[3] as Int,
            backupEnabled = block3[0] as Boolean,
            backupHour = block3[1] as Int,
            backupAccount = block3[2] as String,
            creditLimit = block3[3] as Double,
            appUsers = users,
            backupLogs = logs.map { it.toDomain() },
            currentUserRole = role
        )
    }.combine(settings.darkModeOverride) { state, darkOverride ->
        state.copy(darkModeOverride = darkOverride)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setShopName(name: String) = viewModelScope.launch { settings.setShopName(name) }
    fun setShopMpesa(number: String) = viewModelScope.launch { settings.setShopMpesa(number) }
    fun setMorningPrice(price: Double) = viewModelScope.launch { settings.setMorningPrice(price) }
    fun setEveningPrice(price: Double) = viewModelScope.launch { settings.setEveningPrice(price) }
    fun setTheme(theme: AppTheme) = viewModelScope.launch { settings.setTheme(theme) }
    fun setSmsEnabled(enabled: Boolean) = viewModelScope.launch { settings.setSmsEnabled(enabled) }
    fun setDebtThreshold(amount: Double) = viewModelScope.launch { settings.setDebtAlertThreshold(amount) }
    fun setOverdueDays(days: Int) = viewModelScope.launch { settings.setCustomerOverdueDays(days) }
    fun setBackupEnabled(enabled: Boolean) = viewModelScope.launch { settings.setBackupEnabled(enabled) }
    fun setCreditLimit(limit: Double) = viewModelScope.launch { settings.setCreditLimit(limit) }
    fun setDarkModeOverride(dark: Boolean?) = viewModelScope.launch { settings.setDarkModeOverride(dark) }
    fun setBackupAccount(email: String) = viewModelScope.launch { settings.setBackupAccount(email) }

    fun backUpNow() {
        val workManager = androidx.work.WorkManager.getInstance(context)
        val request = androidx.work.OneTimeWorkRequest.Builder(BackupWorker::class.java).build()
        workManager.enqueue(request)
    }

    fun forceSyncNow() {
        val workManager = androidx.work.WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(
            "SyncWorker_Force",
            androidx.work.ExistingWorkPolicy.REPLACE,
            SyncWorker.buildOneTimeRequest()
        )
    }

    fun fetchDriveBackups() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingBackups.value = true
            try {
                val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
                if (googleAccount != null) {
                    val credential = GoogleAccountCredential.usingOAuth2(
                        context,
                        listOf("https://www.googleapis.com/auth/drive.file")
                    )
                    credential.selectedAccount = googleAccount.account
                    val driveService = Drive.Builder(
                        com.google.api.client.http.javanet.NetHttpTransport(),
                        com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
                        credential
                    ).setApplicationName("Kangai Shop").build()

                    val resultList = driveService.files().list()
                        .setQ("name contains 'kangaishop_backup_' and mimeType = 'application/json' and trashed = false")
                        .setSpaces("drive")
                        .setFields("files(id, name, createdTime, size)")
                        .execute()

                    val files = resultList.files ?: emptyList()
                    val driveFiles = files.map { file ->
                        DriveBackupFile(
                            id = file.id,
                            name = file.name,
                            sizeBytes = file.getSize() ?: 0L,
                            createdTime = file.getCreatedTime()?.value ?: 0L
                        )
                    }.sortedByDescending { it.createdTime }
                    _driveBackups.value = driveFiles
                } else {
                    _driveBackups.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingBackups.value = false
            }
        }
    }

    fun restoreBackup(fileId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _restoreState.value = RestoreState.Loading
            try {
                val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
                if (googleAccount == null) {
                    _restoreState.value = RestoreState.Error("Google account not signed in.")
                    return@launch
                }
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf("https://www.googleapis.com/auth/drive.file")
                )
                credential.selectedAccount = googleAccount.account
                val driveService = Drive.Builder(
                    com.google.api.client.http.javanet.NetHttpTransport(),
                    com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName("Kangai Shop").build()

                val outputStream = java.io.ByteArrayOutputStream()
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                val jsonContent = outputStream.toString("UTF-8")

                BackupHelper.importDatabaseFromJson(
                    jsonContent,
                    personDao,
                    milkDeliveryDao,
                    ledgerTransactionDao,
                    appUserDao,
                    settings
                )
                _restoreState.value = RestoreState.Success
            } catch (e: Exception) {
                _restoreState.value = RestoreState.Error(e.message ?: "Failed to restore backup.")
                e.printStackTrace()
            }
        }
    }

    fun resetRestoreState() {
        _restoreState.value = RestoreState.Idle
    }

    fun addUser(name: String, phone: String, pin: String, role: UserRole) {
        viewModelScope.launch {
            val user = AppUser(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                pin = pin,
                role = role,
                isActive = true
            )
            userRepository.insertUser(user)
        }
    }

    fun resetUserPin(userId: String, newPin: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                val updated = user.copy(pin = ke.eelaminnovations.kangaishop.utils.hashPin(newPin))
                userRepository.updateUser(updated)
            }
        }
    }

    fun toggleUserActive(userId: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                val updated = user.copy(isActive = !user.isActive)
                userRepository.updateUser(updated)
            }
        }
    }
}


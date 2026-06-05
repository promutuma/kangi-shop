package ke.eelaminnovations.kangaishop.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.eelaminnovations.kangaishop.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kangai_settings")

@Singleton
class AppSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.dataStore

    companion object {
        val MORNING_PRICE = doublePreferencesKey("global_milk_price_morning")
        val EVENING_PRICE = doublePreferencesKey("global_milk_price_evening")
        val SHOP_NAME = stringPreferencesKey("shop_name")
        val SHOP_MPESA = stringPreferencesKey("shop_mpesa_number")
        val SMS_ENABLED = booleanPreferencesKey("sms_enabled_global")
        val DEBT_ALERT_THRESHOLD = doublePreferencesKey("debt_alert_threshold")
        val CUSTOMER_OVERDUE_DAYS = intPreferencesKey("customer_overdue_days")
        val BACKUP_ENABLED = booleanPreferencesKey("backup_enabled")
        val BACKUP_HOUR = intPreferencesKey("backup_time_hour")
        val BACKUP_ACCOUNT = stringPreferencesKey("backup_google_account")
        val THEME = stringPreferencesKey("theme")
        val APP_USER_ID = stringPreferencesKey("app_user_id")
        val SHOP_ID = stringPreferencesKey("shop_id")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val CREDIT_LIMIT = doublePreferencesKey("default_credit_limit")
        val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        val DARK_MODE_OVERRIDE = stringPreferencesKey("dark_mode_override") // "LIGHT", "DARK", or "" (follow system)
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val morningPrice: Flow<Double> = store.data.map { it[MORNING_PRICE] ?: 65.0 }
    val eveningPrice: Flow<Double> = store.data.map { it[EVENING_PRICE] ?: 60.0 }
    val shopName: Flow<String> = store.data.map { it[SHOP_NAME] ?: "Kangai Shop" }
    val shopMpesa: Flow<String> = store.data.map { it[SHOP_MPESA] ?: "" }
    val smsEnabled: Flow<Boolean> = store.data.map { it[SMS_ENABLED] ?: true }
    val debtAlertThreshold: Flow<Double> = store.data.map { it[DEBT_ALERT_THRESHOLD] ?: 5000.0 }
    val customerOverdueDays: Flow<Int> = store.data.map { it[CUSTOMER_OVERDUE_DAYS] ?: 7 }
    val backupEnabled: Flow<Boolean> = store.data.map { it[BACKUP_ENABLED] ?: true }
    val backupHour: Flow<Int> = store.data.map { it[BACKUP_HOUR] ?: 2 }
    val backupAccount: Flow<String> = store.data.map { it[BACKUP_ACCOUNT] ?: "" }
    val theme: Flow<AppTheme> = store.data.map {
        try {
            AppTheme.valueOf(it[THEME] ?: AppTheme.DYNAMIC.name)
        } catch (e: Exception) {
            AppTheme.DYNAMIC
        }
    }
    val appUserId: Flow<String> = store.data.map { it[APP_USER_ID] ?: "" }
    val shopId: Flow<String> = store.data.map { it[SHOP_ID] ?: "" }
    val deviceId: Flow<String> = store.data.map { it[DEVICE_ID] ?: "" }
    val creditLimit: Flow<Double> = store.data.map { it[CREDIT_LIMIT] ?: 2000.0 }
    val setupComplete: Flow<Boolean> = store.data.map { it[SETUP_COMPLETE] ?: false }
    val lastSyncTime: Flow<Long> = store.data.map { it[LAST_SYNC_TIME] ?: 0L }

    suspend fun setMorningPrice(price: Double) = store.edit { it[MORNING_PRICE] = price }
    suspend fun setEveningPrice(price: Double) = store.edit { it[EVENING_PRICE] = price }
    suspend fun setShopName(name: String) = store.edit { it[SHOP_NAME] = name }
    suspend fun setShopMpesa(number: String) = store.edit { it[SHOP_MPESA] = number }
    suspend fun setSmsEnabled(enabled: Boolean) = store.edit { it[SMS_ENABLED] = enabled }
    suspend fun setDebtAlertThreshold(amount: Double) = store.edit { it[DEBT_ALERT_THRESHOLD] = amount }
    suspend fun setCustomerOverdueDays(days: Int) = store.edit { it[CUSTOMER_OVERDUE_DAYS] = days }
    suspend fun setBackupEnabled(enabled: Boolean) = store.edit { it[BACKUP_ENABLED] = enabled }
    suspend fun setBackupHour(hour: Int) = store.edit { it[BACKUP_HOUR] = hour }
    suspend fun setBackupAccount(email: String) = store.edit { it[BACKUP_ACCOUNT] = email }
    suspend fun setTheme(theme: AppTheme) = store.edit { it[THEME] = theme.name }
    suspend fun setAppUserId(id: String) = store.edit { it[APP_USER_ID] = id }
    suspend fun setShopId(id: String) = store.edit { it[SHOP_ID] = id }
    suspend fun setDeviceId(id: String) = store.edit { it[DEVICE_ID] = id }
    suspend fun setCreditLimit(limit: Double) = store.edit { it[CREDIT_LIMIT] = limit }
    suspend fun setSetupComplete(complete: Boolean) = store.edit { it[SETUP_COMPLETE] = complete }

    // null = follow system, true = force dark, false = force light
    val darkModeOverride: Flow<Boolean?> = store.data.map {
        when (it[DARK_MODE_OVERRIDE]) {
            "DARK" -> true
            "LIGHT" -> false
            else -> null
        }
    }
    suspend fun setDarkModeOverride(dark: Boolean?) = store.edit {
        it[DARK_MODE_OVERRIDE] = when (dark) {
            true -> "DARK"
            false -> "LIGHT"
            null -> ""
        }
    }
    suspend fun setLastSyncTime(time: Long) = store.edit { it[LAST_SYNC_TIME] = time }
}

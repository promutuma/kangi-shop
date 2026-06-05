package ke.eelaminnovations.kangaishop.utils

import android.util.JsonWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import ke.eelaminnovations.kangaishop.data.local.dao.*
import ke.eelaminnovations.kangaishop.data.local.entity.*
import ke.eelaminnovations.kangaishop.data.settings.AppSettingsDataStore
import ke.eelaminnovations.kangaishop.domain.model.AppTheme
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object BackupHelper {

    suspend fun exportDatabaseToFile(
        file: File,
        personDao: PersonDao,
        milkDeliveryDao: MilkDeliveryDao,
        ledgerTransactionDao: LedgerTransactionDao,
        appUserDao: AppUserDao,
        settings: AppSettingsDataStore
    ) {
        val writer = JsonWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8")).apply {
            setIndent("  ")
        }
        writer.beginObject() // {

        // 1. People
        writer.name("people")
        writer.beginArray()
        for (p in personDao.getAllPeopleForBackup()) {
            writer.beginObject()
            writer.name("id").value(p.id)
            writer.name("name").value(p.name)
            writer.name("phone").value(p.phone)
            writer.name("smsEnabled").value(p.smsEnabled)
            writer.name("notes").value(p.notes)
            writer.name("createdAt").value(p.createdAt)
            writer.name("lastModifiedAt").value(p.lastModifiedAt)
            writer.name("isDeleted").value(p.isDeleted)
            writer.name("syncStatus").value(p.syncStatus)
            writer.name("deviceId").value(p.deviceId)
            writer.endObject()
        }
        writer.endArray()

        // 2. Milk Deliveries
        writer.name("milk_deliveries")
        writer.beginArray()
        for (d in milkDeliveryDao.getAllDeliveriesForBackup()) {
            writer.beginObject()
            writer.name("id").value(d.id)
            writer.name("personId").value(d.personId)
            writer.name("deliveryDate").value(d.deliveryDate)
            writer.name("session").value(d.session)
            writer.name("litres").value(d.litres)
            writer.name("pricePerLitre").value(d.pricePerLitre)
            writer.name("totalValue").value(d.totalValue)
            writer.name("quality").value(d.quality)
            writer.name("rejectedLitres").value(d.rejectedLitres)
            writer.name("notes").value(d.notes)
            writer.name("recordedBy").value(d.recordedBy)
            writer.name("createdAt").value(d.createdAt)
            writer.name("lastModifiedAt").value(d.lastModifiedAt)
            writer.name("isDeleted").value(d.isDeleted)
            writer.name("syncStatus").value(d.syncStatus)
            writer.name("deviceId").value(d.deviceId)
            writer.endObject()
        }
        writer.endArray()

        // 3. Transactions
        writer.name("ledger_transactions")
        writer.beginArray()
        for (t in ledgerTransactionDao.getAllTransactionsForBackup()) {
            writer.beginObject()
            writer.name("id").value(t.id)
            writer.name("personId").value(t.personId)
            writer.name("type").value(t.type)
            writer.name("direction").value(t.direction)
            writer.name("amount").value(t.amount)
            
            writer.name("milkDeliveryId")
            if (t.milkDeliveryId == null) writer.nullValue() else writer.value(t.milkDeliveryId)
            
            writer.name("goodsDescription")
            if (t.goodsDescription == null) writer.nullValue() else writer.value(t.goodsDescription)
            
            writer.name("mpesaRef")
            if (t.mpesaRef == null) writer.nullValue() else writer.value(t.mpesaRef)
            
            writer.name("transactionDate").value(t.transactionDate)
            writer.name("runningBalance").value(t.runningBalance)
            
            writer.name("parentTransactionId")
            if (t.parentTransactionId == null) writer.nullValue() else writer.value(t.parentTransactionId)
            
            writer.name("smsSent").value(t.smsSent)
            
            writer.name("notes")
            if (t.notes == null) writer.nullValue() else writer.value(t.notes)
            
            writer.name("recordedBy").value(t.recordedBy)
            writer.name("createdAt").value(t.createdAt)
            writer.name("lastModifiedAt").value(t.lastModifiedAt)
            writer.name("isDeleted").value(t.isDeleted)
            writer.name("syncStatus").value(t.syncStatus)
            writer.name("deviceId").value(t.deviceId)
            writer.endObject()
        }
        writer.endArray()

        // 4. App Users
        writer.name("app_users")
        writer.beginArray()
        for (u in appUserDao.getAllUsersForBackup()) {
            writer.beginObject()
            writer.name("id").value(u.id)
            writer.name("name").value(u.name)
            writer.name("phone").value(u.phone)
            writer.name("pin").value(u.pin)
            writer.name("role").value(u.role)
            writer.name("isActive").value(u.isActive)
            writer.endObject()
        }
        writer.endArray()

        // 5. Settings
        writer.name("settings")
        writer.beginObject()
        writer.name("global_milk_price_morning").value(settings.morningPrice.first())
        writer.name("global_milk_price_evening").value(settings.eveningPrice.first())
        writer.name("shop_name").value(settings.shopName.first())
        writer.name("shop_mpesa_number").value(settings.shopMpesa.first())
        writer.name("sms_enabled_global").value(settings.smsEnabled.first())
        writer.name("debt_alert_threshold").value(settings.debtAlertThreshold.first())
        writer.name("customer_overdue_days").value(settings.customerOverdueDays.first())
        writer.name("backup_enabled").value(settings.backupEnabled.first())
        writer.name("backup_time_hour").value(settings.backupHour.first())
        writer.name("backup_google_account").value(settings.backupAccount.first())
        writer.name("theme").value(settings.theme.first().name)
        writer.name("app_user_id").value(settings.appUserId.first())
        writer.name("shop_id").value(settings.shopId.first())
        writer.name("device_id").value(settings.deviceId.first())
        writer.name("default_credit_limit").value(settings.creditLimit.first())
        writer.name("setup_complete").value(settings.setupComplete.first())
        writer.name("last_sync_time").value(settings.lastSyncTime.first())
        writer.endObject()

        writer.endObject() // }
        writer.close()
    }

    suspend fun importDatabaseFromJson(
        jsonString: String,
        personDao: PersonDao,
        milkDeliveryDao: MilkDeliveryDao,
        ledgerTransactionDao: LedgerTransactionDao,
        appUserDao: AppUserDao,
        settings: AppSettingsDataStore
    ) {
        val root = JSONObject(jsonString)

        // 1. Import People
        if (root.has("people")) {
            val peopleArray = root.getJSONArray("people")
            val peopleList = mutableListOf<PersonEntity>()
            for (i in 0 until peopleArray.length()) {
                val obj = peopleArray.getJSONObject(i)
                peopleList.add(
                    PersonEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        phone = obj.getString("phone"),
                        role = obj.optString("role", "CONTACT_ONLY"),
                        smsEnabled = obj.getBoolean("smsEnabled"),
                        notes = obj.getString("notes"),
                        createdAt = obj.getLong("createdAt"),
                        lastModifiedAt = obj.getLong("lastModifiedAt"),
                        isDeleted = obj.getBoolean("isDeleted"),
                        syncStatus = obj.getString("syncStatus"),
                        deviceId = obj.getString("deviceId")
                    )
                )
            }
            if (peopleList.isNotEmpty()) {
                personDao.insertPeople(peopleList)
            }
        }

        // 2. Import Milk Deliveries
        if (root.has("milk_deliveries")) {
            val deliveriesArray = root.getJSONArray("milk_deliveries")
            val deliveriesList = mutableListOf<MilkDeliveryEntity>()
            for (i in 0 until deliveriesArray.length()) {
                val obj = deliveriesArray.getJSONObject(i)
                deliveriesList.add(
                    MilkDeliveryEntity(
                        id = obj.getString("id"),
                        personId = obj.getString("personId"),
                        deliveryDate = obj.getLong("deliveryDate"),
                        session = obj.getString("session"),
                        litres = obj.getDouble("litres"),
                        pricePerLitre = obj.getDouble("pricePerLitre"),
                        totalValue = obj.getDouble("totalValue"),
                        quality = obj.getString("quality"),
                        rejectedLitres = obj.getDouble("rejectedLitres"),
                        notes = obj.getString("notes"),
                        recordedBy = obj.getString("recordedBy"),
                        createdAt = obj.getLong("createdAt"),
                        lastModifiedAt = obj.getLong("lastModifiedAt"),
                        isDeleted = obj.getBoolean("isDeleted"),
                        syncStatus = obj.getString("syncStatus"),
                        deviceId = obj.getString("deviceId")
                    )
                )
            }
            if (deliveriesList.isNotEmpty()) {
                milkDeliveryDao.insertDeliveries(deliveriesList)
            }
        }

        // 3. Import Transactions
        if (root.has("ledger_transactions")) {
            val transactionsArray = root.getJSONArray("ledger_transactions")
            val transactionsList = mutableListOf<LedgerTransactionEntity>()
            for (i in 0 until transactionsArray.length()) {
                val obj = transactionsArray.getJSONObject(i)
                transactionsList.add(
                    LedgerTransactionEntity(
                        id = obj.getString("id"),
                        personId = obj.getString("personId"),
                        type = obj.getString("type"),
                        direction = obj.getString("direction"),
                        amount = obj.getDouble("amount"),
                        milkDeliveryId = if (obj.isNull("milkDeliveryId")) null else obj.getString("milkDeliveryId"),
                        goodsDescription = if (obj.isNull("goodsDescription")) null else obj.getString("goodsDescription"),
                        mpesaRef = if (obj.isNull("mpesaRef")) null else obj.getString("mpesaRef"),
                        transactionDate = obj.getLong("transactionDate"),
                        runningBalance = obj.getDouble("runningBalance"),
                        parentTransactionId = if (obj.isNull("parentTransactionId")) null else obj.getString("parentTransactionId"),
                        smsSent = obj.getBoolean("smsSent"),
                        notes = if (obj.isNull("notes")) null else obj.getString("notes"),
                        recordedBy = obj.getString("recordedBy"),
                        createdAt = obj.getLong("createdAt"),
                        lastModifiedAt = obj.getLong("lastModifiedAt"),
                        isDeleted = obj.getBoolean("isDeleted"),
                        syncStatus = obj.getString("syncStatus"),
                        deviceId = obj.getString("deviceId")
                    )
                )
            }
            if (transactionsList.isNotEmpty()) {
                ledgerTransactionDao.insertTransactions(transactionsList)
            }
        }

        // 4. Import App Users
        if (root.has("app_users")) {
            val usersArray = root.getJSONArray("app_users")
            val usersList = mutableListOf<AppUserEntity>()
            for (i in 0 until usersArray.length()) {
                val obj = usersArray.getJSONObject(i)
                usersList.add(
                    AppUserEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        phone = obj.getString("phone"),
                        pin = obj.getString("pin"),
                        role = obj.getString("role"),
                        isActive = obj.getBoolean("isActive")
                    )
                )
            }
            if (usersList.isNotEmpty()) {
                appUserDao.insertUsers(usersList)
            }
        }

        // 5. Import Settings
        if (root.has("settings")) {
            val s = root.getJSONObject("settings")
            if (s.has("global_milk_price_morning")) settings.setMorningPrice(s.getDouble("global_milk_price_morning"))
            if (s.has("global_milk_price_evening")) settings.setEveningPrice(s.getDouble("global_milk_price_evening"))
            if (s.has("shop_name")) settings.setShopName(s.getString("shop_name"))
            if (s.has("shop_mpesa_number")) settings.setShopMpesa(s.getString("shop_mpesa_number"))
            if (s.has("sms_enabled_global")) settings.setSmsEnabled(s.getBoolean("sms_enabled_global"))
            if (s.has("debt_alert_threshold")) settings.setDebtAlertThreshold(s.getDouble("debt_alert_threshold"))
            if (s.has("customer_overdue_days")) settings.setCustomerOverdueDays(s.getInt("customer_overdue_days"))
            if (s.has("backup_enabled")) settings.setBackupEnabled(s.getBoolean("backup_enabled"))
            if (s.has("backup_time_hour")) settings.setBackupHour(s.getInt("backup_time_hour"))
            if (s.has("backup_google_account")) settings.setBackupAccount(s.getString("backup_google_account"))
            if (s.has("theme")) settings.setTheme(AppTheme.valueOf(s.getString("theme")))
            if (s.has("app_user_id")) settings.setAppUserId(s.getString("app_user_id"))
            if (s.has("shop_id")) settings.setShopId(s.getString("shop_id"))
            if (s.has("device_id")) settings.setDeviceId(s.getString("device_id"))
            if (s.has("default_credit_limit")) settings.setCreditLimit(s.getDouble("default_credit_limit"))
            if (s.has("setup_complete")) settings.setSetupComplete(s.getBoolean("setup_complete"))
            if (s.has("last_sync_time")) settings.setLastSyncTime(s.getLong("last_sync_time"))
        }
    }
}

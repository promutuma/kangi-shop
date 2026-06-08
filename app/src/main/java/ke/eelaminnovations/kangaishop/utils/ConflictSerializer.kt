package ke.eelaminnovations.kangaishop.utils

import org.json.JSONObject
import ke.eelaminnovations.kangaishop.data.local.entity.*

object ConflictSerializer {
    fun serializePerson(p: PersonEntity): String = JSONObject().apply {
        put("id", p.id)
        put("name", p.name)
        put("phone", p.phone)
        put("role", p.role)
        put("smsEnabled", p.smsEnabled)
        put("notes", p.notes)
        put("createdAt", p.createdAt)
        put("lastModifiedAt", p.lastModifiedAt)
        put("isDeleted", p.isDeleted)
        put("syncStatus", p.syncStatus)
        put("deviceId", p.deviceId)
    }.toString()

    fun deserializePerson(json: String): PersonEntity {
        val obj = JSONObject(json)
        return PersonEntity(
            obj.getString("id"),
            obj.getString("name"),
            obj.getString("phone"),
            obj.getString("role"),
            obj.getBoolean("smsEnabled"),
            obj.getString("notes"),
            obj.getLong("createdAt"),
            obj.getLong("lastModifiedAt"),
            obj.getBoolean("isDeleted"),
            obj.getString("syncStatus"),
            obj.getString("deviceId")
        )
    }

    fun serializeDelivery(d: MilkDeliveryEntity): String = JSONObject().apply {
        put("id", d.id)
        put("personId", d.personId)
        put("deliveryDate", d.deliveryDate)
        put("session", d.session)
        put("litres", d.litres)
        put("pricePerLitre", d.pricePerLitre)
        put("totalValue", d.totalValue)
        put("quality", d.quality)
        put("rejectedLitres", d.rejectedLitres)
        put("notes", d.notes)
        put("recordedBy", d.recordedBy)
        put("createdAt", d.createdAt)
        put("lastModifiedAt", d.lastModifiedAt)
        put("isDeleted", d.isDeleted)
        put("syncStatus", d.syncStatus)
        put("deviceId", d.deviceId)
    }.toString()

    fun deserializeDelivery(json: String): MilkDeliveryEntity {
        val obj = JSONObject(json)
        return MilkDeliveryEntity(
            obj.getString("id"),
            obj.getString("personId"),
            obj.getLong("deliveryDate"),
            obj.getString("session"),
            obj.getDouble("litres"),
            obj.getDouble("pricePerLitre"),
            obj.getDouble("totalValue"),
            obj.getString("quality"),
            obj.getDouble("rejectedLitres"),
            obj.getString("notes"),
            obj.getString("recordedBy"),
            obj.getLong("createdAt"),
            obj.getLong("lastModifiedAt"),
            obj.getBoolean("isDeleted"),
            obj.getString("syncStatus"),
            obj.getString("deviceId")
        )
    }

    fun serializeTransaction(t: LedgerTransactionEntity): String = JSONObject().apply {
        put("id", t.id)
        put("personId", t.personId)
        put("type", t.type)
        put("direction", t.direction)
        put("amount", t.amount)
        put("milkDeliveryId", t.milkDeliveryId)
        put("goodsDescription", t.goodsDescription)
        put("mpesaRef", t.mpesaRef)
        put("transactionDate", t.transactionDate)
        put("runningBalance", t.runningBalance)
        put("parentTransactionId", t.parentTransactionId)
        put("smsSent", t.smsSent)
        put("notes", t.notes)
        put("recordedBy", t.recordedBy)
        put("createdAt", t.createdAt)
        put("lastModifiedAt", t.lastModifiedAt)
        put("isDeleted", t.isDeleted)
        put("syncStatus", t.syncStatus)
        put("deviceId", t.deviceId)
    }.toString()


    fun deserializeTransaction(json: String): LedgerTransactionEntity {
        val obj = JSONObject(json)
        return LedgerTransactionEntity(
            obj.getString("id"),
            obj.getString("personId"),
            obj.getString("type"),
            obj.getString("direction"),
            obj.getDouble("amount"),
            if (obj.isNull("milkDeliveryId")) null else obj.getString("milkDeliveryId"),
            if (obj.isNull("goodsDescription")) null else obj.getString("goodsDescription"),
            if (obj.isNull("mpesaRef")) null else obj.getString("mpesaRef"),
            obj.getLong("transactionDate"),
            obj.getDouble("runningBalance"),
            if (obj.isNull("parentTransactionId")) null else obj.getString("parentTransactionId"),
            obj.getBoolean("smsSent"),
            if (obj.isNull("notes")) null else obj.getString("notes"),
            obj.getString("recordedBy"),
            obj.getLong("createdAt"),
            obj.getLong("lastModifiedAt"),
            obj.getBoolean("isDeleted"),
            obj.getString("syncStatus"),
            obj.getString("deviceId")
        )
    }
}

// Helper extension to handle null values nicely during JSON generation
private fun JSONObject.optString(key: String, defaultValue: String?): String? {
    return if (this.has(key) && !this.isNull(key)) this.getString(key) else defaultValue
}

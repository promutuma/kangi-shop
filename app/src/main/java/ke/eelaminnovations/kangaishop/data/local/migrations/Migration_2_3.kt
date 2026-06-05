package ke.eelaminnovations.kangaishop.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE people ADD COLUMN role TEXT NOT NULL DEFAULT 'CONTACT_ONLY'")
    }
}

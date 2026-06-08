package ke.eelaminnovations.kangaishop.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `sync_conflicts` (
                `id` TEXT NOT NULL, 
                `entityType` TEXT NOT NULL, 
                `entityId` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `localContent` TEXT NOT NULL, 
                `remoteContent` TEXT NOT NULL, 
                `resolved` INTEGER NOT NULL DEFAULT 0, 
                `createdAt` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}

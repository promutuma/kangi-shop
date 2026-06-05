package ke.eelaminnovations.kangaishop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ke.eelaminnovations.kangaishop.domain.model.BackupLog
import ke.eelaminnovations.kangaishop.domain.model.BackupStatus

@Entity(tableName = "backup_log")
data class BackupLogEntity(
    @PrimaryKey val id: String,
    val backupDate: Long,
    val status: String,
    val driveFileId: String?,
    val fileSizeBytes: Long,
    val errorMessage: String?
) {
    fun toDomain() = BackupLog(
        id = id,
        backupDate = backupDate,
        status = BackupStatus.valueOf(status),
        driveFileId = driveFileId,
        fileSizeBytes = fileSizeBytes,
        errorMessage = errorMessage
    )
}

fun BackupLog.toEntity() = BackupLogEntity(
    id = id,
    backupDate = backupDate,
    status = status.name,
    driveFileId = driveFileId,
    fileSizeBytes = fileSizeBytes,
    errorMessage = errorMessage
)

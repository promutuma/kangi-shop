package ke.eelaminnovations.kangaishop.domain.model

data class BackupLog(
    val id: String,
    val backupDate: Long,
    val status: BackupStatus,
    val driveFileId: String? = null,
    val fileSizeBytes: Long = 0,
    val errorMessage: String? = null
)

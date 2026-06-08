package ke.eelaminnovations.kangaishop.ui.settings

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import ke.eelaminnovations.kangaishop.domain.model.AppTheme
import ke.eelaminnovations.kangaishop.domain.model.BackupLog
import ke.eelaminnovations.kangaishop.domain.model.BackupStatus
import ke.eelaminnovations.kangaishop.domain.model.AppUser
import ke.eelaminnovations.kangaishop.domain.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToSmsLog: () -> Unit,
    onNavigateToConflicts: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val driveBackups by viewModel.driveBackups.collectAsStateWithLifecycle()
    val isLoadingDriveBackups by viewModel.isLoadingBackups.collectAsStateWithLifecycle()
    val restoreState by viewModel.restoreState.collectAsStateWithLifecycle()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedRestoreFile by remember { mutableStateOf<DriveBackupFile?>(null) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }

    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedUserForAction by remember { mutableStateOf<AppUser?>(null) }
    var showUserActionDialog by remember { mutableStateOf(false) }
    var showResetPinDialog by remember { mutableStateOf(false) }

    // Google Sign-In setup
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
        .build()
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email ?: ""
                viewModel.setBackupAccount(email)
                Toast.makeText(context, "Connected to: $email", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Sign-in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Handle restore state updates
    LaunchedEffect(restoreState) {
        when (restoreState) {
            is RestoreState.Success -> {
                Toast.makeText(context, "Database restored successfully!", Toast.LENGTH_LONG).show()
                viewModel.resetRestoreState()
                showRestoreDialog = false
            }
            is RestoreState.Error -> {
                Toast.makeText(context, "Restore failed: ${(restoreState as RestoreState.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.resetRestoreState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (uiState.currentUserRole == UserRole.ATTENDANT) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔒 Access Restricted", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Settings are restricted to Owner accounts.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Shop Info
            SettingsSection(title = "Shop Info", icon = Icons.Default.Storefront) {
                SettingsTextField(label = "Shop name", value = uiState.shopName, onValueChange = viewModel::setShopName)
                SettingsTextField(label = "M-Pesa number / till", value = uiState.shopMpesa, onValueChange = viewModel::setShopMpesa, keyboardType = KeyboardType.Phone)
            }

            // Milk Pricing
            SettingsSection(title = "Milk Pricing", icon = Icons.Default.LocalDrink) {
                SettingsTextField(label = "Morning default price (KES/L)", value = uiState.morningPrice.toString(), onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setMorningPrice(v) } }, keyboardType = KeyboardType.Decimal)
                SettingsTextField(label = "Evening default price (KES/L)", value = uiState.eveningPrice.toString(), onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setEveningPrice(v) } }, keyboardType = KeyboardType.Decimal)
                Text("Both are editable per delivery at record time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            // Appearance
            SettingsSection(title = "Appearance", icon = Icons.Default.DarkMode) {
                Text("Display Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("System" to null, "Light" to false, "Dark" to true).forEach { (label, value) ->
                        FilterChip(
                            selected = uiState.darkModeOverride == value,
                            onClick = { viewModel.setDarkModeOverride(value) },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Theme Selection", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Select a color palette theme for the application. The dynamic theme will use your system wallpaper colors (Android 12+).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                
                // Let's lay out the themes in a flowing grid-like arrangement or a clear list for premium aesthetics
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.entries.forEach { theme ->
                        val isSelected = uiState.theme == theme
                        val themeColor = when (theme) {
                            AppTheme.DYNAMIC -> MaterialTheme.colorScheme.primary
                            AppTheme.BRAND_BLUE -> Color(0xFF1A56A0)
                            AppTheme.FOREST_GREEN -> Color(0xFF2E7D32)
                            AppTheme.SUNSET_GOLD -> Color(0xFFE65100)
                            AppTheme.OCEAN_BREEZE -> Color(0xFF00796B)
                            AppTheme.LAVENDER_FIELD -> Color(0xFF673AB7)
                            AppTheme.CRIMSON_VELVET -> Color(0xFF880E4F)
                            AppTheme.CHARCOAL_MINIMAL -> Color(0xFF37474F)
                            AppTheme.SAKURA_BLOSSOM -> Color(0xFFEC407A)
                            AppTheme.MIDNIGHT_NAVY -> Color(0xFF0D47A1)
                        }
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTheme(theme) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(themeColor)
                                )
                            },
                            label = { 
                                Text(
                                    text = theme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // SMS
            SettingsSection(title = "SMS Notifications", icon = Icons.Default.Sms) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SMS Notifications", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = uiState.smsEnabled, onCheckedChange = viewModel::setSmsEnabled)
                }
                TextButton(onClick = onNavigateToSmsLog) {
                    Text("View SMS Log & Retry Failed")
                }
            }

            // Alerts
            SettingsSection(title = "Alert Thresholds", icon = Icons.Default.NotificationsActive) {
                SettingsTextField(label = "Debt alert threshold (KES)", value = uiState.debtAlertThreshold.toString(), onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setDebtThreshold(v) } }, keyboardType = KeyboardType.Decimal)
                SettingsTextField(label = "Customer overdue (days)", value = uiState.customerOverdueDays.toString(), onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setOverdueDays(v) } }, keyboardType = KeyboardType.Number)
                SettingsTextField(label = "Default credit limit (KES)", value = uiState.creditLimit.toString(), onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setCreditLimit(v) } }, keyboardType = KeyboardType.Decimal)
            }

            // Backup
            SettingsSection(title = "Google Drive Backup", icon = Icons.Default.Cloud) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto backup (Daily)", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = uiState.backupEnabled, onCheckedChange = viewModel::setBackupEnabled)
                }

                // Sign In / Out UI
                if (uiState.backupAccount.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Google account connected for backups", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        val signInIntent = googleSignInClient.signInIntent
                                        googleSignInLauncher.launch(signInIntent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect Google Account")
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Backup Account", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(uiState.backupAccount, style = MaterialTheme.typography.bodyMedium)
                            }
                            TextButton(
                                onClick = {
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        viewModel.setBackupAccount("")
                                        Toast.makeText(context, "Google account disconnected", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Disconnect", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Backup Status Indicator
                val latestSuccess = uiState.backupLogs.firstOrNull { it.status == BackupStatus.SUCCESS }
                val backupStatusColor = when {
                    latestSuccess == null -> MaterialTheme.colorScheme.error
                    System.currentTimeMillis() - latestSuccess.backupDate < 24 * 3600 * 1000 -> Color(0xFF2E7D32)
                    System.currentTimeMillis() - latestSuccess.backupDate < 48 * 3600 * 1000 -> Color(0xFFE65100)
                    else -> MaterialTheme.colorScheme.error
                }
                val backupStatusText = when {
                    latestSuccess == null -> "No successful backups found"
                    else -> "Last synced: " + SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(latestSuccess.backupDate))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(backupStatusColor, CircleShape)
                    )
                    Text(backupStatusText, style = MaterialTheme.typography.bodyMedium)
                }

                if (uiState.backupAccount.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.backUpNow()
                                Toast.makeText(context, "Backup worker triggered", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back Up Now")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.fetchDriveBackups()
                                showRestoreDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore DB")
                        }
                    }
                }

                // Local Backup History Logs
                if (uiState.backupLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Backup History Logs", style = MaterialTheme.typography.titleSmall)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        uiState.backupLogs.take(10).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val dateStr = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(log.backupDate))
                                    Text(dateStr, style = MaterialTheme.typography.bodySmall)
                                    if (log.status == BackupStatus.FAILED) {
                                        Text(log.errorMessage ?: "Failed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (log.status == BackupStatus.SUCCESS) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = log.status.name,
                                        color = if (log.status == BackupStatus.SUCCESS) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // App Users
            SettingsSection(title = "App Users", icon = Icons.Default.People) {
                uiState.appUsers.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedUserForAction = user
                                showUserActionDialog = true
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(user.role.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        if (!user.isActive) {
                            Text("Inactive", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showAddUserDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add User")
                }
            }

            // Sync
            val conflicts by viewModel.unresolvedConflicts.collectAsStateWithLifecycle()
            SettingsSection(title = "Sync & Conflicts", icon = Icons.Default.Sync) {
                if (conflicts.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Sync Conflicts Found", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("There are ${conflicts.size} unresolved sync conflicts.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                            }
                            Button(
                                onClick = onNavigateToConflicts,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Resolve")
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = viewModel::forceSyncNow, modifier = Modifier.weight(1f)) { Text("Force Sync Now") }
                }
            }


            // About
            SettingsSection(title = "About", icon = Icons.Default.Info) {
                Text("Kangai Shop v1.0", style = MaterialTheme.typography.bodyMedium)
                Text("Built by Eelam Innovations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { if (restoreState !is RestoreState.Loading) showRestoreDialog = false },
            title = { Text("Restore database from Google Drive") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (restoreState is RestoreState.Loading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        Text("Downloading and restoring backup...", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (isLoadingDriveBackups) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        Text("Fetching files from Google Drive...", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (driveBackups.isEmpty()) {
                        Text("No backup files found on Google Drive.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("Select a backup file to restore. This will overwrite local data.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            driveBackups.forEach { file ->
                                val dateStr = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(file.createdTime))
                                val sizeStr = formatFileSize(file.sizeBytes)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                                        .clickable {
                                            selectedRestoreFile = file
                                            showConfirmRestoreDialog = true
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.name, style = MaterialTheme.typography.bodyMedium)
                                        Text("$dateStr • $sizeStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                if (restoreState !is RestoreState.Loading) {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Confirm Restore Dialog
    if (showConfirmRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDialog = false },
            title = { Text("Confirm Restore") },
            text = {
                Text(
                    "Are you sure you want to restore the backup file: '${selectedRestoreFile?.name}'?\n\nWARNING: This will replace your entire local database with the contents of the backup. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedRestoreFile?.id?.let { fileId ->
                            viewModel.restoreBackup(fileId)
                        }
                        showConfirmRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Overwrite & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add User Dialog
    if (showAddUserDialog) {
        var newUserName by remember { mutableStateOf("") }
        var newUserPhone by remember { mutableStateOf("") }
        var newUserPin by remember { mutableStateOf("") }
        var newUserRole by remember { mutableStateOf(UserRole.ATTENDANT) }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("Add New User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsTextField(label = "Full Name", value = newUserName, onValueChange = { newUserName = it })
                    SettingsTextField(label = "Phone Number", value = newUserPhone, onValueChange = { newUserPhone = it }, keyboardType = KeyboardType.Phone)
                    SettingsTextField(label = "4-Digit PIN", value = newUserPin, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newUserPin = it }, keyboardType = KeyboardType.Number)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Role", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = newUserRole == UserRole.ATTENDANT, onClick = { newUserRole = UserRole.ATTENDANT })
                            Text("Attendant")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = newUserRole == UserRole.OWNER, onClick = { newUserRole = UserRole.OWNER })
                            Text("Owner")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUserName.isNotEmpty() && newUserPhone.isNotEmpty() && newUserPin.length == 4) {
                            viewModel.addUser(newUserName, newUserPhone, newUserPin, newUserRole)
                            showAddUserDialog = false
                        } else {
                            Toast.makeText(context, "Please fill all fields. PIN must be 4 digits.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Create User")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // User Actions Dialog (Reset PIN / Deactivate)
    val userForAction = selectedUserForAction
    if (showUserActionDialog && userForAction != null) {
        val user = userForAction
        AlertDialog(
            onDismissRequest = { showUserActionDialog = false },
            title = { Text("Manage User: ${user.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Role: ${user.role.name.lowercase().replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)
                    Text("Status: ${if (user.isActive) "Active" else "Deactivated"}", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Button(
                        onClick = {
                            showUserActionDialog = false
                            showResetPinDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset PIN")
                    }
                    val actionText = if (user.isActive) "Deactivate" else "Activate"
                    OutlinedButton(
                        onClick = {
                            viewModel.toggleUserActive(user.id)
                            showUserActionDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (user.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    ) {
                        Text(actionText)
                    }
                    TextButton(onClick = { showUserActionDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Reset PIN Dialog
    val userForReset = selectedUserForAction
    if (showResetPinDialog && userForReset != null) {
        val user = userForReset
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showResetPinDialog = false },
            title = { Text("Reset PIN for ${user.name}") },
            text = {
                Column {
                    SettingsTextField(label = "New 4-Digit PIN", value = newPin, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it }, keyboardType = KeyboardType.Number)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            viewModel.resetUserPin(user.id, newPin)
                            showResetPinDialog = false
                            Toast.makeText(context, "PIN updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "PIN must be exactly 4 digits.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

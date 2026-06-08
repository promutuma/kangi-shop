package ke.eelaminnovations.kangaishop.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.eelaminnovations.kangaishop.data.local.entity.SyncConflictEntity
import ke.eelaminnovations.kangaishop.utils.ConflictSerializer
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictResolutionScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val conflicts by viewModel.unresolvedConflicts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resolve Sync Conflicts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (conflicts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉 All clear!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No unresolved conflicts found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(conflicts, key = { it.id }) { conflict ->
                    ConflictCard(
                        conflict = conflict,
                        onKeepLocal = { viewModel.resolveConflict(conflict, keepLocal = true) },
                        onKeepRemote = { viewModel.resolveConflict(conflict, keepLocal = false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConflictCard(
    conflict: SyncConflictEntity,
    onKeepLocal: () -> Unit,
    onKeepRemote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = conflict.entityType,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(conflict.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Text(conflict.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Local values
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text("Local Device", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    ConflictFieldFormatter(conflict.entityType, conflict.localContent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onKeepLocal,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Keep Local", fontSize = 12.sp)
                    }
                }

                // Remote values
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text("Cloud Server", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    ConflictFieldFormatter(conflict.entityType, conflict.remoteContent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onKeepRemote,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Keep Remote", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConflictFieldFormatter(entityType: String, contentJson: String) {
    val lines = remember(entityType, contentJson) {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val obj = JSONObject(contentJson)
            when (entityType) {
                "PERSON" -> {
                    list.add("Name" to obj.optString("name", ""))
                    list.add("Phone" to obj.optString("phone", ""))
                    list.add("Role" to obj.optString("role", ""))
                    list.add("SMS" to if (obj.optBoolean("smsEnabled", true)) "Enabled" else "Disabled")
                }
                "DELIVERY" -> {
                    list.add("Session" to obj.optString("session", ""))
                    list.add("Litres" to obj.optDouble("litres", 0.0).toString())
                    list.add("Price" to obj.optDouble("pricePerLitre", 0.0).toString() + " KES")
                    list.add("Total" to obj.optDouble("totalValue", 0.0).toString() + " KES")
                }
                "TRANSACTION" -> {
                    list.add("Type" to obj.optString("type", ""))
                    list.add("Direction" to obj.optString("direction", ""))
                    list.add("Amount" to obj.optDouble("amount", 0.0).toString() + " KES")
                    if (obj.has("mpesaRef") && !obj.isNull("mpesaRef")) {
                        list.add("M-Pesa" to obj.optString("mpesaRef", ""))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    if (lines.isEmpty()) {
        Text("Invalid payload content", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            lines.forEach { (lbl, valStr) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(lbl, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(valStr, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


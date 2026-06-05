package ke.eelaminnovations.kangaishop.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.eelaminnovations.kangaishop.domain.model.SmsLog
import ke.eelaminnovations.kangaishop.domain.model.SmsStatus
import ke.eelaminnovations.kangaishop.utils.formatShortDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsLogScreen(
    onBack: () -> Unit,
    viewModel: SmsLogViewModel = hiltViewModel()
) {
    val logs by viewModel.smsLogs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS History") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No SMS logs found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs) { log ->
                    SmsLogItem(log = log, onRetry = { viewModel.retryFailedSms(log) })
                }
            }
        }
    }
}

@Composable
private fun SmsLogItem(log: SmsLog, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (log.status == SmsStatus.FAILED) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(log.recipientName, fontWeight = FontWeight.Bold)
                Text(formatShortDate(log.sentAt), style = MaterialTheme.typography.bodySmall)
            }
            Text("Phone: ${log.recipientPhone}", style = MaterialTheme.typography.bodyMedium)
            Text(log.message, style = MaterialTheme.typography.bodyMedium)

            if (log.status == SmsStatus.FAILED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Failed: ${log.errorMessage ?: "Unknown error"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onRetry) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry")
                        Spacer(Modifier.width(4.dp))
                        Text("Retry")
                    }
                }
            } else {
                Text("Status: Sent Successfully", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

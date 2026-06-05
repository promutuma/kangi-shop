package ke.eelaminnovations.kangaishop.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.eelaminnovations.kangaishop.ui.components.ListSkeleton
import ke.eelaminnovations.kangaishop.ui.components.RecordDeliverySheet
import ke.eelaminnovations.kangaishop.ui.components.SyncStatusBar
import ke.eelaminnovations.kangaishop.ui.components.bounceClick
import ke.eelaminnovations.kangaishop.ui.navigation.Screen
import ke.eelaminnovations.kangaishop.ui.theme.WarningOrange
import ke.eelaminnovations.kangaishop.domain.model.UserRole
import ke.eelaminnovations.kangaishop.utils.formatKes
import ke.eelaminnovations.kangaishop.utils.formatLitres
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeRoute(onNavigate: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState = uiState, onNavigate = onNavigate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigate: (String) -> Unit
) {
    if (uiState is HomeUiState.Loading) {
        Scaffold(topBar = { TopAppBar(title = { Text("Kangai Shop") }) }) { padding ->
            Box(Modifier.padding(padding).padding(16.dp)) { ListSkeleton(count = 4) }
        }
        return
    }
    val summary = (uiState as HomeUiState.Success).data
    var showMorningSheet by remember { mutableStateOf(false) }
    var showEveningSheet by remember { mutableStateOf(false) }
    val dateStr = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kangai Shop", fontWeight = FontWeight.Bold) },
                actions = {
                    SyncStatusBar()
                    if (summary.currentUserRole == UserRole.OWNER) {
                        IconButton(onClick = { onNavigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header greeting Card
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "$greeting, ${summary.currentUserName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // ================= BENTO GRID STRUCTURE =================

            // Bento Card 1: Combined Daily Milk Yield Hero (100% Width)
            val combinedYield = summary.morningLitres + summary.eveningLitres
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick {
                        // Spring-animated click to open primary delivery route/action
                        showMorningSheet = true
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Intake Today",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.LocalDrink,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Text(
                            text = formatLitres(combinedYield),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            "Tap to record morning delivery",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Bento Grid Row 2: Asymmetric layout of Morning & Evening yields (50/50 balance details)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MilkSummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WbSunny,
                    label = "Morning Yield",
                    value = formatLitres(summary.morningLitres),
                    warning = false,
                    onClick = { showMorningSheet = true }
                )
                MilkSummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.NightsStay,
                    label = "Evening Yield",
                    value = if (summary.eveningRecorded) formatLitres(summary.eveningLitres) else "Pending",
                    warning = summary.eveningWarning,
                    onClick = { showEveningSheet = true }
                )
            }

            // Bento Grid Row 3: Asymmetric Ledger Balances (66% / 33% Bento layout logic)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Owed Out to Suppliers (Primary 60% Bento weight)
                FinanceSummaryCard(
                    modifier = Modifier.weight(1.2f),
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Shop owes suppliers",
                    amount = summary.totalOwedOut,
                    sub = "Total supplier payables",
                    onClick = { if (summary.currentUserRole == UserRole.OWNER) onNavigate(Screen.Reports.route) }
                )
                // Owed In from Customers (Ancillary 40% Bento weight)
                FinanceSummaryCard(
                    modifier = Modifier.weight(0.8f),
                    icon = Icons.Default.CreditCard,
                    label = "Owed in",
                    amount = summary.totalOwedIn,
                    sub = "Credit balance",
                    onClick = { if (summary.currentUserRole == UserRole.OWNER) onNavigate(Screen.Reports.route) }
                )
            }

            // Alerts Panel (Only visible if attention is required)
            val hasAlerts = summary.highBalanceAlerts.isNotEmpty() || summary.eveningWarning || summary.overdueCustomers.isNotEmpty()
            if (hasAlerts) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Needs Attention", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        if (summary.eveningWarning) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Evening milk delivery has not yet been recorded", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        summary.highBalanceAlerts.forEach { (person, balance) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("${person.name} is owed: ${formatKes(balance)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        summary.overdueCustomers.forEach { (person, _) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("${person.name} has overdue credit balance", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Bento Grid Row 4: Quick Action spring-loaded Bento blocks
            Text("Quick Operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "+ Morning Delivery",
                        onClick = { showMorningSheet = true }
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "+ Evening Delivery",
                        onClick = { showEveningSheet = true }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (summary.currentUserRole == UserRole.OWNER) {
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            text = "Record Payment",
                            onClick = { onNavigate(Screen.People.route) }
                        )
                    }
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "Issue Credit",
                        onClick = { onNavigate(Screen.People.route) }
                    )
                }
            }
        }
    }

    if (showMorningSheet) {
        RecordDeliverySheet(
            isMorning = true,
            onDismiss = { showMorningSheet = false },
            onSaved = { showMorningSheet = false }
        )
    }
    if (showEveningSheet) {
        RecordDeliverySheet(
            isMorning = false,
            onDismiss = { showEveningSheet = false },
            onSaved = { showEveningSheet = false }
        )
    }
}

@Composable
private fun MilkSummaryCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    warning: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .bounceClick(onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (warning) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (warning) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FinanceSummaryCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    amount: Double,
    sub: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .bounceClick(onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatKes(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun QuickActionButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(56.dp)
            .bounceClick(onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

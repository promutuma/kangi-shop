package ke.eelaminnovations.kangaishop.ui.milk

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import ke.eelaminnovations.kangaishop.ui.components.bounceClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.eelaminnovations.kangaishop.domain.model.DailySupplierSummary
import ke.eelaminnovations.kangaishop.ui.components.ListSkeleton
import ke.eelaminnovations.kangaishop.ui.components.RecordDeliverySheet
import ke.eelaminnovations.kangaishop.ui.components.SyncStatusBar
import ke.eelaminnovations.kangaishop.utils.formatKes
import ke.eelaminnovations.kangaishop.utils.formatLitres
import kotlinx.coroutines.launch

// Route: wires ViewModel
@Composable
fun MilkRoute(onNavigateToLedger: (String) -> Unit, viewModel: MilkViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MilkScreen(
        uiState = uiState,
        onNavigateToLedger = onNavigateToLedger,
        onRecordDelivery = viewModel::recordDelivery,
        onUndoDelivery = viewModel::undoLastDelivery,
        onClearUndo = viewModel::clearUndo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkScreen(
    uiState: MilkUiState,
    onNavigateToLedger: (String) -> Unit,
    onRecordDelivery: (person: ke.eelaminnovations.kangaishop.domain.model.Person, isMorning: Boolean, litres: Double, pricePerLitre: Double, quality: ke.eelaminnovations.kangaishop.domain.model.MilkQuality, rejectedLitres: Double, notes: String, deliveryDate: Long, onSaved: (String, String) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onUndoDelivery: () -> Unit = {},
    onClearUndo: () -> Unit = {}
) {
    if (uiState is MilkUiState.Loading) {
        Scaffold(topBar = { TopAppBar(title = { Text("Milk") }) }) { padding ->
            Box(Modifier.padding(padding).padding(16.dp)) { ListSkeleton(count = 3) }
        }
        return
    }
    val data = (uiState as MilkUiState.Success).data
    var showMorningSheet by remember { mutableStateOf(false) }
    var showEveningSheet by remember { mutableStateOf(false) }
    val snackState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val totalLitres = data.dailySummaries.sumOf { it.totalLitres }
    val totalValue = data.dailySummaries.sumOf { it.totalValue }

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) },
        topBar = {
            TopAppBar(
                title = { Text("Milk Deliveries", fontWeight = FontWeight.Bold) },
                actions = {
                    SyncStatusBar()
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bounceClick { showMorningSheet = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Morning 🌅", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bounceClick { showEveningSheet = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Evening 🌇", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Aggregated Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(formatLitres(totalLitres), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(formatKes(totalValue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
 
            if (data.dailySummaries.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No deliveries today", style = MaterialTheme.typography.titleMedium)
                        Text("Tap Morning or Evening below to record the first delivery.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                items(data.dailySummaries) { summary ->
                    SupplierDayCard(summary = summary, onClick = { onNavigateToLedger(summary.person.id) })
                }
            }
        }
    }

    fun handleSaved(message: String) {
        scope.launch {
            val result = snackState.showSnackbar(
                message = message,
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoDelivery()
            } else {
                onClearUndo()
            }
        }
    }

    if (showMorningSheet) {
        RecordDeliverySheet(
            isMorning = true,
            onDismiss = { showMorningSheet = false },
            onSaved = { message ->
                showMorningSheet = false
                handleSaved(message)
            }
        )
    }
    if (showEveningSheet) {
        RecordDeliverySheet(
            isMorning = false,
            onDismiss = { showEveningSheet = false },
            onSaved = { message ->
                showEveningSheet = false
                handleSaved(message)
            }
        )
    }
}

@Composable
private fun SupplierDayCard(summary: DailySupplierSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(summary.person.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                summary.morningDelivery?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Morning: ${formatLitres(it.litres)} · ${formatKes(it.totalValue)}", style = MaterialTheme.typography.bodyMedium)
                    }
                } ?: Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Morning: pending", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
 
                summary.eveningDelivery?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Evening: ${formatLitres(it.litres)} · ${formatKes(it.totalValue)}", style = MaterialTheme.typography.bodyMedium)
                    }
                } ?: Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Evening: pending", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total: ${formatLitres(summary.totalLitres)} = ${formatKes(summary.totalValue)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                val balance = summary.netBalance
                Text(
                    text = if (balance > 0) "Owed ${formatKes(balance)}" else "Settled",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (balance > 5000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

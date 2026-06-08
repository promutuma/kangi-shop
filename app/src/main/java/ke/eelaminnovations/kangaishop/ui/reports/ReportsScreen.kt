package ke.eelaminnovations.kangaishop.ui.reports

import android.content.Intent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import ke.eelaminnovations.kangaishop.ui.components.bounceClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import ke.eelaminnovations.kangaishop.domain.model.Person
import ke.eelaminnovations.kangaishop.domain.model.MilkDelivery
import ke.eelaminnovations.kangaishop.domain.model.UserRole

import ke.eelaminnovations.kangaishop.utils.PdfHelper
import ke.eelaminnovations.kangaishop.utils.formatKes
import ke.eelaminnovations.kangaishop.utils.formatLitres

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Route: wires ViewModel
@Composable
fun ReportsRoute(onNavigateToLedger: (String) -> Unit, viewModel: ReportsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportsScreen(
        uiState = uiState,
        onNavigateToLedger = onNavigateToLedger,
        onSetPeriod = viewModel::setPeriod,
        getDeliveriesForDay = viewModel::getDeliveriesForDay
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    uiState: ReportSummary,
    onNavigateToLedger: (String) -> Unit,
    onSetPeriod: (ReportPeriod) -> Unit = {},
    getDeliveriesForDay: (Long) -> kotlinx.coroutines.flow.Flow<List<Pair<MilkDelivery, Person>>> = { kotlinx.coroutines.flow.flowOf(emptyList()) }
) {
    val context = LocalContext.current
    var selectedDayStart by remember { mutableStateOf<Long?>(null) }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                actions = {
                    if (!uiState.isLoading) {
                        IconButton(
                            onClick = {
                                try {
                                    val file = PdfHelper.generateReportPdf(context, uiState)
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Report PDF"))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF")
                        }
                    }
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
                            text = "Reports are restricted to Owner accounts.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
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
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = uiState.period == period,
                            onClick = { onSetPeriod(period) },
                            label = { Text(period.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                item { ke.eelaminnovations.kangaishop.ui.components.ListSkeleton(count = 3) }
            } else {
                // Milk received summary
                item {
                    ReportCard(title = "Milk Received Summary") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatLitres(uiState.totalMilkLitres), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(formatKes(uiState.totalMilkValue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🌅 Morning: ${formatLitres(uiState.morningLitres)}", style = MaterialTheme.typography.bodyMedium)
                            Text("🌇 Evening: ${formatLitres(uiState.eveningLitres)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Daily Morning vs Evening Stacked Bar Chart
                if (uiState.dailyMilk.isNotEmpty()) {
                    item {
                        ReportCard(title = "Daily Delivery (Morning vs Evening split)") {
                            Text("Tap a bar to see detail breakdown.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(Modifier.height(4.dp))
                            AndroidView(
                                factory = { ctx ->
                                    BarChart(ctx).apply {
                                        description.isEnabled = false
                                        setDrawGridBackground(false)
                                        setDrawBarShadow(false)
                                        setDrawValueAboveBar(false)
                                        xAxis.apply {
                                            granularity = 1f
                                            setDrawGridLines(false)
                                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                        }
                                        axisLeft.apply {
                                            setDrawGridLines(true)
                                            axisMinimum = 0f
                                        }
                                        axisRight.isEnabled = false
                                        legend.isEnabled = true
                                        setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                                            override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                                                e?.let {
                                                    val index = it.x.toInt()
                                                    if (index >= 0 && index < uiState.dailyMilk.size) {
                                                        selectedDayStart = uiState.dailyMilk[index].dayStart
                                                    }
                                                }
                                            }
                                            override fun onNothingSelected() {}
                                        })
                                    }
                                },
                                update = { chart ->
                                    val entries = uiState.dailyMilk.mapIndexed { index, agg ->
                                        BarEntry(index.toFloat(), floatArrayOf(agg.morningLitres.toFloat(), agg.eveningLitres.toFloat()))
                                    }
                                    val dataSet = BarDataSet(entries, "Litres").apply {
                                        colors = listOf(
                                            android.graphics.Color.parseColor("#4CAF50"), // Morning (Green)
                                            android.graphics.Color.parseColor("#FFC107")  // Evening (Amber)
                                        )
                                        stackLabels = arrayOf("Morning", "Evening")
                                        setDrawValues(false)
                                    }
                                    chart.data = BarData(dataSet)
                                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(uiState.dailyMilk.map { it.dayLabel })
                                    chart.invalidate()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }
                }

                // Monthly Milk production trend line
                if (uiState.monthlyMilk.isNotEmpty()) {
                    item {
                        ReportCard(title = "Monthly Production Trend (Last 6 Months)") {
                            AndroidView(
                                factory = { ctx ->
                                    LineChart(ctx).apply {
                                        description.isEnabled = false
                                        setDrawGridBackground(false)
                                        xAxis.apply {
                                            granularity = 1f
                                            setDrawGridLines(false)
                                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                        }
                                        axisLeft.apply {
                                            setDrawGridLines(true)
                                            axisMinimum = 0f
                                        }
                                        axisRight.isEnabled = false
                                        legend.isEnabled = false
                                    }
                                },
                                update = { chart ->
                                    val entries = uiState.monthlyMilk.mapIndexed { index, agg ->
                                        Entry(index.toFloat(), agg.totalLitres.toFloat())
                                    }
                                    val dataSet = LineDataSet(entries, "Litres").apply {
                                        color = android.graphics.Color.parseColor("#2E7D32")
                                        setCircleColor(android.graphics.Color.parseColor("#2E7D32"))
                                        lineWidth = 2f
                                        circleRadius = 4f
                                        setDrawCircleHole(false)
                                        valueTextSize = 9f
                                    }
                                    chart.data = LineData(dataSet)
                                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(uiState.monthlyMilk.map { it.monthLabel })
                                    chart.invalidate()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }




                // Supplier debts
                item {
                    ReportCard(title = "Supplier Debts") {
                        Text("Total owed out: ${formatKes(uiState.totalOwedOut)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        uiState.supplierBalances.take(5).forEach { (person, balance) ->
                            BalanceRow(person = person, balance = balance, isDebt = balance > 0, onClick = { onNavigateToLedger(person.id) })
                        }
                    }
                }

                // Customer credit
                item {
                    ReportCard(title = "Customer Credit") {
                        Text("Total owed in: ${formatKes(uiState.totalOwedIn)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        uiState.customerBalances.take(5).forEach { (person, balance) ->
                            BalanceRow(person = person, balance = balance, isDebt = balance > 0, onClick = { onNavigateToLedger(person.id) })
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet Drilldown for selected day
    val dayStartVal = selectedDayStart
    if (dayStartVal != null) {
        val details by getDeliveriesForDay(dayStartVal).collectAsStateWithLifecycle(initialValue = emptyList())
        ModalBottomSheet(
            onDismissRequest = { selectedDayStart = null },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val dayLabel = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(dayStartVal))
                Text(
                    text = "Intake Breakdown: $dayLabel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (details.isEmpty()) {
                    Text("No deliveries recorded for this day.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(details.size) { index ->
                            val (delivery, supplier) = details[index]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(supplier.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "${delivery.session.name.lowercase().replaceFirstChar { it.uppercase() }} • Quality: ${delivery.quality.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(formatLitres(delivery.litres), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(formatKes(delivery.totalValue), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

}

@Composable
private fun ReportCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(20.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun BalanceRow(person: Person, balance: Double, isDebt: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .bounceClick(onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(person.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${formatKes(balance)}${if (balance > 5000) " 🔴" else if (balance > 2000) " 🟠" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance > 5000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

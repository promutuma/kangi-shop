package ke.eelaminnovations.kangaishop.ui.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import ke.eelaminnovations.kangaishop.ui.components.bounceClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.eelaminnovations.kangaishop.domain.model.*
import ke.eelaminnovations.kangaishop.ui.theme.PositiveGreen
import ke.eelaminnovations.kangaishop.ui.theme.NegativeRed
import ke.eelaminnovations.kangaishop.utils.formatBalanceLabel
import ke.eelaminnovations.kangaishop.utils.formatKes
import ke.eelaminnovations.kangaishop.utils.formatShortDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonLedgerScreen(
    personId: String,
    onBack: () -> Unit,
    viewModel: PersonLedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showCreditSheet by remember { mutableStateOf(false) }
    var showGoodsSheet by remember { mutableStateOf(false) }
    var showEditPersonSheet by remember { mutableStateOf(false) }
    var showDeletePersonConfirm by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<LedgerTransaction?>(null) }
    val snackState = remember { SnackbarHostState() }

    val snackIsUndoable by viewModel.snackIsUndoable.collectAsStateWithLifecycle()

    LaunchedEffect(personId) { viewModel.loadPerson(personId) }
    LaunchedEffect(uiState.snackMessage) {
        uiState.snackMessage?.let { msg ->
            if (snackIsUndoable) {
                val result = snackState.showSnackbar(
                    message = msg,
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoLastTransaction()
                } else {
                    viewModel.clearSnack()
                }
            } else {
                snackState.showSnackbar(msg)
                viewModel.clearSnack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.person?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (uiState.currentUserRole == UserRole.OWNER) {
                        IconButton(onClick = { showEditPersonSheet = true }) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                        IconButton(onClick = { showDeletePersonConfirm = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete Profile") }
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.currentUserRole == UserRole.OWNER) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .bounceClick { showPaymentSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Text("Payment", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .bounceClick { showCreditSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Credit to Shop", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .bounceClick { showGoodsSheet = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Goods Purchase on Credit", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.person?.let { person ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(person.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (uiState.role) {
                            PersonRole.SUPPLIER -> {
                                Icon(Icons.Default.LocalDrink, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text("Supplier", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            }
                            PersonRole.CUSTOMER -> {
                                Icon(Icons.Default.People, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text("Customer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            }
                            PersonRole.BOTH -> {
                                Icon(Icons.Default.LocalDrink, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text("Supplier", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                                Text("·", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                                Icon(Icons.Default.People, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text("Customer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            }
                            PersonRole.CONTACT_ONLY -> {
                                Text("Contact only", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            item {
                val isUrgent = uiState.netBalance > 5000
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUrgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isUrgent) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Net Balance", style = MaterialTheme.typography.labelSmall, color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        Text(
                            text = formatBalanceLabel(uiState.netBalance, uiState.person?.name ?: ""),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LedgerFilter.values().forEach { filter ->
                        FilterChip(
                            selected = uiState.filter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("DATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("DETAILS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("KES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                HorizontalDivider()
            }

            if (uiState.isLoading) {
                item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else if (uiState.currentUserRole == UserRole.ATTENDANT) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Detailed transaction history is restricted to Owner accounts.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (uiState.transactions.isEmpty()) {
                item {
                    Text(
                        "No transactions in this period.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {

                items(uiState.transactions) { tx ->
                    LedgerRow(
                        transaction = tx,
                        isOwner = uiState.currentUserRole == UserRole.OWNER,
                        onClick = { transactionToDelete = tx }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        }
    }

    if (showPaymentSheet) {
        RecordPaymentSheet(
            personName = uiState.person?.name ?: "",
            currentBalance = uiState.netBalance,
            onDismiss = { showPaymentSheet = false },
            onSave = { type, amount, mpesaRef, goodsDescription, notes ->
                viewModel.recordPayment(type, amount, mpesaRef, goodsDescription, notes)
                showPaymentSheet = false
            }
        )
    }

    if (showCreditSheet) {
        RecordCreditSheet(
            personName = uiState.person?.name ?: "",
            personRole = uiState.role,
            currentBalance = uiState.netBalance,
            creditLimit = uiState.creditLimit,
            onDismiss = { showCreditSheet = false },
            onSave = { description, amount, notes ->
                viewModel.recordCredit(description, amount, notes)
                showCreditSheet = false
            }
        )
    }

    if (showGoodsSheet) {
        RecordGoodsSheet(
            personName = uiState.person?.name ?: "",
            currentBalance = uiState.netBalance,
            onDismiss = { showGoodsSheet = false },
            onSave = { description, amount, notes ->
                viewModel.recordGoods(description, amount, notes)
                showGoodsSheet = false
            }
        )
    }

    if (showEditPersonSheet) {
        PersonBottomSheet(
            personId = personId,
            onDismiss = {
                showEditPersonSheet = false
                viewModel.loadPerson(personId) // reload details
            }
        )
    }

    if (showDeletePersonConfirm) {
        val personName = uiState.person?.name ?: ""
        ModalBottomSheet(onDismissRequest = { showDeletePersonConfirm = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delete Profile",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Delete $personName's profile? This cannot be undone.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeletePersonConfirm = false },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            showDeletePersonConfirm = false
                            viewModel.deletePerson {
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }

    transactionToDelete?.let { tx ->
        val personName = uiState.person?.name ?: ""
        val typeLabel = when (tx.type) {
            TransactionType.MILK_DELIVERY -> "milk delivery"
            TransactionType.PAYMENT_CASH, TransactionType.PAYMENT_MPESA, TransactionType.PAYMENT_GOODS -> "payment"
            TransactionType.CREDIT_ISSUED -> "credit"
            TransactionType.GOODS_ON_CREDIT -> "goods purchase"
            TransactionType.CUSTOMER_PAYMENT_CASH, TransactionType.CUSTOMER_PAYMENT_MPESA -> "payment"
        }
        val amountStr = formatKes(tx.amount)
        val dateStr = formatShortDate(tx.transactionDate)

        ModalBottomSheet(onDismissRequest = { transactionToDelete = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delete Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Delete $personName's $typeLabel of $amountStr on $dateStr? This cannot be undone.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { transactionToDelete = null },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.deleteTransaction(tx)
                            transactionToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerRow(
    transaction: LedgerTransaction,
    isOwner: Boolean,
    onClick: () -> Unit
) {
    val (icon, label) = transactionLabel(transaction)
    val isDebit = transaction.direction == TransactionDirection.DEBIT
    val amountColor = if (isDebit) PositiveGreen else NegativeRed
    val amountPrefix = if (isDebit) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isOwner, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(formatShortDate(transaction.transactionDate), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(48.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
        Text(
            text = "$amountPrefix${formatKes(transaction.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = amountColor
        )
    }
}

private fun transactionLabel(tx: LedgerTransaction): Pair<androidx.compose.ui.graphics.vector.ImageVector, String> = when (tx.type) {
    TransactionType.MILK_DELIVERY -> Icons.Default.LocalDrink to "Milk delivery"
    TransactionType.PAYMENT_CASH -> Icons.Default.AttachMoney to "Cash paid"
    TransactionType.PAYMENT_MPESA -> Icons.Default.Payment to "M-Pesa paid"
    TransactionType.PAYMENT_GOODS -> Icons.Default.ShoppingCart to "Goods: ${tx.goodsDescription ?: ""}"
    TransactionType.CREDIT_ISSUED -> Icons.Default.ShoppingBag to "Credit: ${tx.goodsDescription ?: ""}"
    TransactionType.GOODS_ON_CREDIT -> Icons.Default.Inventory2 to "Bought: ${tx.goodsDescription ?: ""}"
    TransactionType.CUSTOMER_PAYMENT_CASH -> Icons.Default.AttachMoney to "Cash payment received"
    TransactionType.CUSTOMER_PAYMENT_MPESA -> Icons.Default.Payment to "M-Pesa payment received"
}

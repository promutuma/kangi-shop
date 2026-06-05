package ke.eelaminnovations.kangaishop.ui.people

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import ke.eelaminnovations.kangaishop.domain.model.TransactionType
import ke.eelaminnovations.kangaishop.utils.formatKes

enum class PaymentMethod { CASH, MPESA, GOODS }

data class GoodsItem(val description: String = "", val amount: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentSheet(
    personName: String,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onSave: (type: TransactionType, amount: Double, mpesaRef: String?, goodsDesc: String?, notes: String?) -> Unit
) {
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var amount by remember { mutableStateOf("") }
    var mpesaRef by remember { mutableStateOf("") }
    var goodsItems by remember { mutableStateOf(listOf(GoodsItem())) }
    var notes by remember { mutableStateOf("") }

    val effectiveAmount = when (method) {
        PaymentMethod.GOODS -> goodsItems.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        else -> amount.toDoubleOrNull() ?: 0.0
    }
    val newBalance = currentBalance - effectiveAmount

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Record Payment — $personName", style = MaterialTheme.typography.titleLarge)
            Text("Shop owes: ${formatKes(currentBalance)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Text("Payment Method *", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PaymentMethod.values().forEach { m ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = method == m, onClick = { method = m })
                        Text(
                            text = when (m) { PaymentMethod.CASH -> "Cash"; PaymentMethod.MPESA -> "M-Pesa"; PaymentMethod.GOODS -> "Goods from Shop" },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            when (method) {
                PaymentMethod.CASH, PaymentMethod.MPESA -> {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (KES) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (method == PaymentMethod.MPESA) {
                        OutlinedTextField(
                            value = mpesaRef,
                            onValueChange = { mpesaRef = it },
                            label = { Text("M-Pesa Reference") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                PaymentMethod.GOODS -> {
                    Text("Items from Shop", style = MaterialTheme.typography.labelLarge)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        goodsItems.forEachIndexed { index, item ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = item.description,
                                    onValueChange = { desc ->
                                        goodsItems = goodsItems.toMutableList().apply {
                                            this[index] = item.copy(description = desc)
                                        }
                                    },
                                    label = { Text("Item description") },
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = item.amount,
                                    onValueChange = { amt ->
                                        goodsItems = goodsItems.toMutableList().apply {
                                            this[index] = item.copy(amount = amt)
                                        }
                                    },
                                    label = { Text("Amount") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                if (goodsItems.size > 1) {
                                    IconButton(onClick = {
                                        goodsItems = goodsItems.toMutableList().apply { removeAt(index) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Item")
                                    }
                                }
                            }
                        }
                    }
                    TextButton(onClick = { goodsItems = goodsItems + GoodsItem() }) {
                        Text("+ Add another item")
                    }
                }
            }

            if (effectiveAmount > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        text = "${formatKes(currentBalance)} − ${formatKes(effectiveAmount)} = ${formatKes(newBalance)}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Button(
                onClick = {
                    val txType = when (method) {
                        PaymentMethod.CASH -> TransactionType.PAYMENT_CASH
                        PaymentMethod.MPESA -> TransactionType.PAYMENT_MPESA
                        PaymentMethod.GOODS -> TransactionType.PAYMENT_GOODS
                    }
                    val finalDesc = if (method == PaymentMethod.GOODS) {
                        goodsItems.filter { it.description.isNotBlank() }
                            .joinToString(", ") { "${it.description} (${formatKes(it.amount.toDoubleOrNull() ?: 0.0)})" }
                    } else {
                        null
                    }
                    onSave(txType, effectiveAmount, mpesaRef.ifBlank { null }, finalDesc, notes.ifBlank { null })
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = effectiveAmount > 0
            ) {
                Text("SAVE PAYMENT")
            }
        }
    }
}


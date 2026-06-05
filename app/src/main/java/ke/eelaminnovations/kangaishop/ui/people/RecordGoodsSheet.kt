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
import ke.eelaminnovations.kangaishop.utils.formatKes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordGoodsSheet(
    personName: String,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onSave: (description: String, amount: Double, notes: String?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val amountVal = amount.toDoubleOrNull() ?: 0.0
    val newBalance = currentBalance + amountVal

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Record Goods Purchase", style = MaterialTheme.typography.titleLarge)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "$personName takes products from shop on credit",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "$personName currently owes shop: ${formatKes(currentBalance)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("What products? (e.g., 2kg sugar, cooking oil)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (KES) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (amountVal > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "${formatKes(currentBalance)} + ${formatKes(amountVal)} = ${formatKes(newBalance)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "$personName will now owe: ${formatKes(newBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
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
                onClick = { onSave(description, amountVal, notes.ifBlank { null }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = description.isNotBlank() && amountVal > 0
            ) {
                Text("RECORD PURCHASE")
            }
        }
    }
}

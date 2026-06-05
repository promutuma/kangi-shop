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
import ke.eelaminnovations.kangaishop.domain.model.PersonRole
import ke.eelaminnovations.kangaishop.utils.formatKes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordCreditSheet(
    personName: String,
    personRole: PersonRole = PersonRole.CONTACT_ONLY,
    currentBalance: Double,
    creditLimit: Double = 2000.0,
    onDismiss: () -> Unit,
    onSave: (description: String, amount: Double, notes: String?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val amountVal = amount.toDoubleOrNull() ?: 0.0
    val newBalance = currentBalance + amountVal
    val overLimit = newBalance > creditLimit

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val (title, explanation, fieldLabel) = when (personRole) {
                PersonRole.SUPPLIER -> Triple(
                    "Advance Payment — $personName",
                    "You are advancing payment to this milk supplier. This reduces what you owe them.",
                    "What are you advancing payment for?"
                )
                PersonRole.CUSTOMER -> Triple(
                    "Issue Store Credit — $personName",
                    "$personName can now buy from shop on credit and pay later.",
                    "What did they take on credit?"
                )
                PersonRole.BOTH -> Triple(
                    "Issue Credit — $personName",
                    "Record credit transaction (as both supplier and customer).",
                    "Describe this credit transaction"
                )
                else -> Triple(
                    "Record Credit — $personName",
                    "Record a credit transaction.",
                    "Describe this credit"
                )
            }

            Text(title, style = MaterialTheme.typography.titleLarge)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(explanation, style = MaterialTheme.typography.bodyMedium)

                    val balanceText = when (personRole) {
                        PersonRole.SUPPLIER -> "Shop currently owes: ${formatKes(currentBalance)}"
                        PersonRole.CUSTOMER -> "$personName currently owes shop: ${formatKes(currentBalance)}"
                        else -> "Current balance: ${formatKes(currentBalance)}"
                    }
                    Text(balanceText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }

            if (creditLimit > 0 && personRole == PersonRole.CUSTOMER) {
                val usedPercent = (currentBalance / creditLimit).coerceIn(0.0, 1.0).toFloat()
                Text("Credit limit: ${formatKes(creditLimit)}", style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(progress = { usedPercent }, modifier = Modifier.fillMaxWidth())
                Text("${(usedPercent * 100).toInt()}% used", style = MaterialTheme.typography.labelSmall, color = if (overLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(fieldLabel) },
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
                Card(colors = CardDefaults.cardColors(containerColor = if (overLimit) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${formatKes(currentBalance)} + ${formatKes(amountVal)} = ${formatKes(newBalance)}", style = MaterialTheme.typography.titleMedium)
                        if (overLimit) {
                            Text("⚠ This exceeds the credit limit of ${formatKes(creditLimit)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = description.isNotBlank() && amountVal > 0
            ) {
                val buttonText = when (personRole) {
                    PersonRole.SUPPLIER -> "SAVE ADVANCE PAYMENT"
                    PersonRole.CUSTOMER -> "ISSUE CREDIT"
                    else -> "SAVE CREDIT"
                }
                Text(buttonText)
            }
        }
    }
}

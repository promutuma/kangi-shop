package ke.eelaminnovations.kangaishop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.eelaminnovations.kangaishop.domain.model.MilkQuality
import ke.eelaminnovations.kangaishop.domain.model.Person
import ke.eelaminnovations.kangaishop.ui.milk.MilkViewModel
import ke.eelaminnovations.kangaishop.utils.formatFullDate
import ke.eelaminnovations.kangaishop.utils.formatKes
import ke.eelaminnovations.kangaishop.utils.formatLitres

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDeliverySheet(
    isMorning: Boolean,
    preselectedPersonId: String? = null,
    onDismiss: () -> Unit,
    onSaved: (message: String) -> Unit,
    viewModel: MilkViewModel = hiltViewModel()
) {
    val uiStateRaw by viewModel.uiState.collectAsStateWithLifecycle()
    val uiState = (uiStateRaw as? ke.eelaminnovations.kangaishop.ui.milk.MilkUiState.Success)?.data
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var litres by remember { mutableStateOf("") }
    var priceOverride by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf(MilkQuality.GOOD) }
    var rejectedLitres by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showPersonSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Person>>(emptyList()) }

    val sessionLabel = if (isMorning) "Morning" else "Evening"
    val defaultPrice = if (isMorning) uiState?.morningDefaultPrice ?: 65.0 else uiState?.eveningDefaultPrice ?: 60.0
    val effectivePrice = priceOverride.toDoubleOrNull() ?: defaultPrice
    val litresVal = litres.toDoubleOrNull() ?: 0.0
    val total = litresVal * effectivePrice

    LaunchedEffect(preselectedPersonId, uiState?.people) {
        if (preselectedPersonId != null) {
            selectedPerson = uiState?.people?.find { it.id == preselectedPersonId }
        }
    }

    // Collect search results reactively
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = uiState?.people ?: emptyList()
        } else {
            viewModel.searchPeople(searchQuery).collect { results ->
                searchResults = results
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isMorning) Icons.Default.WbSunny else Icons.Default.NightsStay,
                    contentDescription = null,
                    tint = if (isMorning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Text("Record $sessionLabel Session", style = MaterialTheme.typography.titleLarge)
            }

            // Supplier selector
            OutlinedCard(
                onClick = { showPersonSearch = !showPersonSearch },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedPerson?.name ?: "Select supplier...")
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            if (showPersonSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search supplier") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                searchResults.take(6).forEach { person ->
                    TextButton(
                        onClick = {
                            selectedPerson = person
                            showPersonSearch = false
                            searchQuery = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(person.name, color = MaterialTheme.colorScheme.onSurface)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(person.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = formatFullDate(System.currentTimeMillis()),
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = litres,
                onValueChange = { litres = it },
                label = { Text("Litres Received *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = priceOverride,
                onValueChange = { priceOverride = it },
                label = { Text("Price per Litre (KES)") },
                placeholder = { Text("$defaultPrice") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (litresVal > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        text = "${formatLitres(litresVal)} × KES $effectivePrice = ${formatKes(total)}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text("Quality", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MilkQuality.entries.forEach { q ->
                    FilterChip(
                        selected = quality == q,
                        onClick = { quality = q },
                        label = { Text(q.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (quality == MilkQuality.PARTIAL) {
                OutlinedTextField(
                    value = rejectedLitres,
                    onValueChange = { rejectedLitres = it },
                    label = { Text("Rejected Litres") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Button(
                onClick = {
                    if (selectedPerson != null && litresVal > 0) {
                        saving = true
                        viewModel.recordDelivery(
                            person = selectedPerson!!,
                            isMorning = isMorning,
                            litres = litresVal,
                            pricePerLitre = effectivePrice,
                            quality = quality,
                            rejectedLitres = rejectedLitres.toDoubleOrNull() ?: 0.0,
                            notes = notes,
                            onSaved = { _, message -> onSaved(message) }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !saving && selectedPerson != null && litresVal > 0
            ) {
                if (saving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("SAVE DELIVERY")
            }
        }
    }
}

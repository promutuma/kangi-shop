package ke.eelaminnovations.kangaishop.ui.people

import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.eelaminnovations.kangaishop.domain.model.PersonRole

data class DeviceContact(val name: String, val phone: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonBottomSheet(
    personId: String? = null,
    onDismiss: () -> Unit,
    viewModel: AddPersonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEdit = personId != null
    val title = if (isEdit) "Edit Profile" else "Add New Person"
    val context = LocalContext.current

    var deviceContacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    var showContactList by remember { mutableStateOf(false) }
    var contactSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        deviceContacts = loadDeviceContacts(context)
    }

    val filteredContacts = deviceContacts.filter { contact ->
        contact.name.contains(contactSearchQuery, ignoreCase = true) ||
        contact.phone.contains(contactSearchQuery)
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val number = cursor.getString(numberIndex).replace(" ", "").replace("-", "")
                    val name = cursor.getString(nameIndex)
                    viewModel.setPhone(number)
                    if (uiState.name.isBlank()) {
                        viewModel.setName(name)
                    }
                    showContactList = false
                }
            }
        }
    }

    fun selectContact(contact: DeviceContact) {
        viewModel.setPhone(contact.phone.replace(" ", "").replace("-", ""))
        if (uiState.name.isBlank()) {
            viewModel.setName(contact.name)
        }
        showContactList = false
        contactSearchQuery = ""
    }

    LaunchedEffect(personId) {
        if (personId != null) {
            viewModel.loadPerson(personId)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::setName,
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column {
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { phone ->
                        viewModel.setPhone(phone)
                        showContactList = phone.isNotEmpty()
                    },
                    label = { Text("Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (uiState.phone.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewModel.setPhone("")
                                        showContactList = false
                                        contactSearchQuery = ""
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                    contactPickerLauncher.launch(intent)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "System Contacts", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    isError = uiState.duplicatePerson != null,
                    supportingText = {
                        if (uiState.duplicatePerson != null) {
                            Text(
                                text = "This number belongs to ${uiState.duplicatePerson!!.name}",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (uiState.phone.length >= 10) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Manual entry", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                )

                if (showContactList && filteredContacts.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(filteredContacts) { contact ->
                                ContactListItem(
                                    contact = contact,
                                    onClick = { selectContact(contact) }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Send SMS Notifications", style = MaterialTheme.typography.bodyLarge)
                    Text("Auto send transaction receipts via SMS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Switch(checked = uiState.smsEnabled, onCheckedChange = viewModel::setSmsEnabled)
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::setNotes,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Text(
                text = "Role",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                PersonRole.values().forEachIndexed { index, role ->
                    SegmentedButton(
                        selected = uiState.role == role,
                        onClick = { viewModel.setRole(role) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = PersonRole.values().size
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            when (role) {
                                PersonRole.SUPPLIER -> "Milk Supplier"
                                PersonRole.CUSTOMER -> "Customer"
                                PersonRole.BOTH -> "Both"
                                PersonRole.CONTACT_ONLY -> "Contact"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Text(
                text = when (uiState.role) {
                    PersonRole.SUPPLIER -> "Shop receives milk from them • you owe them for deliveries"
                    PersonRole.CUSTOMER -> "They buy from your shop • they owe you for purchases"
                    PersonRole.BOTH -> "Both milk supplier and customer"
                    PersonRole.CONTACT_ONLY -> "Phone contact only, not involved in transactions"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !uiState.isSaving && uiState.name.isNotBlank() && uiState.phone.isNotBlank() && uiState.duplicatePerson == null
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (isEdit) "Save" else "Create")
                    }
                }
            }
        }
    }
}

@Composable
fun ContactListItem(contact: DeviceContact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(contact.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 12.dp))
}

fun loadDeviceContacts(context: android.content.Context): List<DeviceContact> {
    val contacts = mutableListOf<DeviceContact>()
    try {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val phone = cursor.getString(phoneIndex)
                if (name != null && phone != null) {
                    contacts.add(DeviceContact(name, phone))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return contacts
}

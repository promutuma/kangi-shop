package ke.eelaminnovations.kangaishop.ui.people

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import ke.eelaminnovations.kangaishop.ui.components.bounceClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.eelaminnovations.kangaishop.domain.model.PersonRole
import ke.eelaminnovations.kangaishop.domain.model.PersonWithRole
import ke.eelaminnovations.kangaishop.domain.model.UserRole
import ke.eelaminnovations.kangaishop.ui.components.SyncStatusBar
import ke.eelaminnovations.kangaishop.utils.formatKes

// Route: wires ViewModel
@Composable
fun PeopleRoute(
    onNavigateToLedger: (String) -> Unit,
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddPersonSheet by remember { mutableStateOf(false) }

    PeopleScreen(
        uiState = uiState,
        onNavigateToLedger = onNavigateToLedger,
        onNavigateToAddPerson = { showAddPersonSheet = true },
        onSetFilter = viewModel::setFilter,
        onSearch = viewModel::setSearchQuery
    )

    if (showAddPersonSheet) {
        PersonBottomSheet(
            onDismiss = { showAddPersonSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    uiState: PeopleUiState,
    onNavigateToLedger: (String) -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onSetFilter: (PeopleFilter) -> Unit = {},
    onSearch: (String) -> Unit = {}
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("People") },
                actions = {
                    SyncStatusBar()
                }
            )
        },
        floatingActionButton = {
            if (uiState.currentUserRole == UserRole.OWNER) {
                FloatingActionButton(onClick = onNavigateToAddPerson) {
                    Icon(Icons.Default.Add, contentDescription = "Add Person")
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearch,
                    label = { Text("Search by name or number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search people") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PeopleFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = uiState.filter == filter,
                            onClick = { onSetFilter(filter) },
                            label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                item { ke.eelaminnovations.kangaishop.ui.components.ListSkeleton(count = 4) }
            } else if (uiState.people.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No people yet", style = MaterialTheme.typography.titleMedium)
                        Text("Add a person or record a delivery to add one automatically.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                items(uiState.people) { personWithRole ->
                    PersonCard(personWithRole = personWithRole, onClick = { onNavigateToLedger(personWithRole.person.id) })
                }
            }
        }
    }
}

@Composable
fun PersonCard(personWithRole: PersonWithRole, onClick: () -> Unit) {
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                    Text(personWithRole.person.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                val roleLabel = when (personWithRole.role) {
                    PersonRole.SUPPLIER -> "Supplier"
                    PersonRole.CUSTOMER -> "Customer"
                    PersonRole.BOTH -> "Both"
                    PersonRole.CONTACT_ONLY -> "Contact"
                }
                val roleIcon = when (personWithRole.role) {
                    PersonRole.SUPPLIER -> Icons.Default.LocalCafe
                    PersonRole.CUSTOMER -> Icons.Default.People
                    PersonRole.BOTH -> Icons.Default.People
                    PersonRole.CONTACT_ONLY -> Icons.Default.Person
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = roleIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(roleLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Text(personWithRole.person.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            
            val balance = personWithRole.netBalance
            val balanceText = when {
                balance > 0.01 -> "Net: Shop owes ${formatKes(balance)}"
                balance < -0.01 -> "Net: Owes shop ${formatKes(-balance)}"
                else -> "Net: Settled"
            }
            val balanceColor = when {
                balance > 5000 -> MaterialTheme.colorScheme.error
                balance < -0.01 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            }
            Text(balanceText, style = MaterialTheme.typography.bodyMedium, color = balanceColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

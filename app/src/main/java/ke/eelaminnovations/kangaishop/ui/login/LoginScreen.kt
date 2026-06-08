package ke.eelaminnovations.kangaishop.ui.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Fingerprint
import androidx.activity.compose.LocalActivity
import androidx.fragment.app.FragmentActivity
import ke.eelaminnovations.kangaishop.domain.model.AppUser
import ke.eelaminnovations.kangaishop.domain.model.UserRole
import ke.eelaminnovations.kangaishop.ui.components.bounceClick
import ke.eelaminnovations.kangaishop.utils.BiometricAuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNeedsSetup: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as FragmentActivity
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) onLoginSuccess()
    }
    LaunchedEffect(uiState.needsSetup) {
        if (uiState.needsSetup) onNeedsSetup()
    }

    LaunchedEffect(uiState.showBiometric) {
        if (uiState.showBiometric) {
            val biometricManager = BiometricAuthManager(activity)
            val result = biometricManager.authenticate()
            when (result) {
                BiometricAuthManager.BiometricResult.Success -> viewModel.onBiometricSuccess()
                else -> viewModel.onBiometricFailed()
            }
        }
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Content: Store Info and Profile Selection
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Kangai Shop",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Select your profile to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))

            // Profiles list
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(uiState.users) { user ->
                    val isSelected = uiState.selectedUser?.id == user.id
                    UserProfileCard(
                        user = user,
                        isSelected = isSelected,
                        onClick = {
                            viewModel.selectUser(user)
                        }
                    )
                }
            }
        }

        // Responsive Bottom Sheet for PIN Entry / Biometric verification
        if (uiState.selectedUser != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.clearSelection() },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .widthIn(max = 480.dp) // Maintain premium tablet look
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter PIN for ${uiState.selectedUser!!.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (uiState.selectedUser!!.role == UserRole.OWNER) "Owner Profile" else "Attendant Profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(24.dp))

                    // PIN Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            val isActive = index < uiState.pin.length
                            val dotSize by animateDpAsState(
                                targetValue = if (isActive) 16.dp else 12.dp,
                                animationSpec = spring(
                                    dampingRatio = 0.6f,
                                    stiffness = 300f
                                ),
                                label = "dotSize"
                            )
                            val dotColor by animateColorAsState(
                                targetValue = when {
                                    uiState.pinError -> MaterialTheme.colorScheme.error
                                    isActive -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                                animationSpec = tween(durationMillis = 150),
                                label = "dotColor"
                            )
                            Box(
                                modifier = Modifier.size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dotSize)
                                        .background(dotColor, shape = CircleShape)
                                )
                            }
                        }
                    }

                    if (uiState.pinError) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Wrong PIN. Try again.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // Numeric Keypad
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("biometric", "0", "⌫")
                        ).forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                row.forEach { label ->
                                    if (label == "biometric") {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .bounceClick {
                                                    viewModel.selectUser(uiState.selectedUser!!)
                                                }
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = CircleShape
                                                )
                                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Biometric Authentication",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    } else if (label.isEmpty()) {
                                        Spacer(Modifier.size(72.dp))
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .bounceClick {
                                                    if (label == "⌫") viewModel.onPinDelete() else viewModel.onPinDigit(label)
                                                }
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    shape = CircleShape
                                                )
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    user: AppUser,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = if (isSelected) {
            listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        }
    )

    val borderStrokeWidth = if (isSelected) 3.dp else 1.dp
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 160.dp)
            .bounceClick(onClick)
            .background(
                brush = gradientBrush,
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = borderStrokeWidth,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (user.role == UserRole.OWNER) Icons.Default.WorkspacePremium else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (user.role == UserRole.OWNER) "Owner" else "Attendant",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}

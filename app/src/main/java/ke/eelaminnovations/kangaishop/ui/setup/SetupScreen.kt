package ke.eelaminnovations.kangaishop.ui.setup

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import ke.eelaminnovations.kangaishop.ui.components.bounceClick

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.setupComplete) {
        if (uiState.setupComplete) onSetupComplete()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepProgressIndicator(currentStep = uiState.step)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (uiState.step) {
                    SetupStep.SHOP_INFO -> ShopInfoStep(
                        shopName = uiState.shopName,
                        ownerName = uiState.ownerName,
                        onShopNameChange = viewModel::setShopName,
                        onOwnerNameChange = viewModel::setOwnerName,
                        onNext = viewModel::nextStep
                    )
                    SetupStep.CREATE_PIN -> CreatePinStep(
                        pin = uiState.pin,
                        confirmPin = uiState.confirmPin,
                        pinMismatch = uiState.pinMismatch,
                        isSaving = uiState.isSaving,
                        onDigit = viewModel::onPinDigit,
                        onDelete = viewModel::onPinDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun StepProgressIndicator(currentStep: SetupStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val activeColor = MaterialTheme.colorScheme.primary
        val inactiveColor = MaterialTheme.colorScheme.outlineVariant

        Text(
            text = "1. Shop Details",
            style = MaterialTheme.typography.labelLarge,
            color = if (currentStep == SetupStep.SHOP_INFO) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = if (currentStep == SetupStep.SHOP_INFO) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.width(48.dp),
            color = if (currentStep == SetupStep.CREATE_PIN) activeColor else inactiveColor,
            thickness = 2.dp
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "2. Create PIN",
            style = MaterialTheme.typography.labelLarge,
            color = if (currentStep == SetupStep.CREATE_PIN) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = if (currentStep == SetupStep.CREATE_PIN) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ShopInfoStep(
    shopName: String,
    ownerName: String,
    onShopNameChange: (String) -> Unit,
    onOwnerNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Welcome to Kangai Shop",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Let's configure your store profile",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = onShopNameChange,
                    label = { Text("Shop Name") },
                    placeholder = { Text("e.g. Kangai Shop") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = onOwnerNameChange,
                    label = { Text("Your Name (Owner)") },
                    placeholder = { Text("e.g. Mama Wanjiku") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        val isEnabled = shopName.isNotBlank() && ownerName.isNotBlank()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .bounceClick { if (isEnabled) onNext() }
                .background(
                    color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NEXT: CREATE PIN →",
                color = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CreatePinStep(
    pin: String,
    confirmPin: String,
    pinMismatch: Boolean,
    isSaving: Boolean,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isConfirming = pin.length == 4
    val label = if (!isConfirming) "Create a 4-digit PIN" else "Confirm your PIN"
    val currentEntry = if (!isConfirming) pin else confirmPin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "You'll use this PIN every time you open the app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { i ->
                val isActive = i < currentEntry.length
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
                        pinMismatch -> MaterialTheme.colorScheme.error
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                    animationSpec = tween(durationMillis = 150),
                    label = "dotColor"
                )
                Box(
                    modifier = Modifier
                        .size(24.dp),
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

        if (pinMismatch) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "PINs don't match. Try again.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(40.dp))

        if (isSaving) {
            Box(
                modifier = Modifier.height(356.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                ).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        row.forEach { label ->
                            if (label.isEmpty()) {
                                Spacer(Modifier.size(72.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .bounceClick {
                                            if (label == "⌫") onDelete() else onDigit(label)
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
        }
    }
}

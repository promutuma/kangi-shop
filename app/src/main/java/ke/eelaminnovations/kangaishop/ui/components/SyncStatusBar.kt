package ke.eelaminnovations.kangaishop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SyncStatusBar(
    viewModel: SyncStatusViewModel = hiltViewModel()
) {
    val state by viewModel.syncState.collectAsStateWithLifecycle()
    
    val icon = when (state) {
        SyncState.SYNCED -> Icons.Default.CloudDone
        SyncState.SYNCING -> Icons.Default.Sync
        SyncState.PENDING -> Icons.Default.Sync
        SyncState.OFFLINE -> Icons.Default.CloudOff
    }
    val label = when (state) {
        SyncState.SYNCED -> "Synced"
        SyncState.SYNCING -> "Syncing..."
        SyncState.PENDING -> "Pending"
        SyncState.OFFLINE -> "Offline"
    }
    val tint = when (state) {
        SyncState.SYNCED -> MaterialTheme.colorScheme.primary
        SyncState.SYNCING -> MaterialTheme.colorScheme.primary
        SyncState.PENDING -> MaterialTheme.colorScheme.secondary
        SyncState.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    val angle = if (state == SyncState.SYNCING) {
        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "angle"
        ).value
    } else {
        0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier
                .size(16.dp)
                .rotate(angle)
        )
        Text(text = label, fontSize = 14.sp, color = tint)
    }
}

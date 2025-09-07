package me.seta.vacset.qrwari.presentation.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable 
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
// import androidx.compose.ui.tooling.preview.Preview // No longer needed
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Unused imports related to Preview code, remove if not used elsewhere in this file after Preview removal.
// import me.seta.vacset.qrwari.data.repository.EventHistoryRepository
// import me.seta.vacset.qrwari.data.storage.EventDao
// import me.seta.vacset.qrwari.data.storage.EventEntity
import me.seta.vacset.qrwari.presentation.state.EventHistoryItemUiState
import me.seta.vacset.qrwari.presentation.state.EventHistoryViewModel

private val HistoryItemCardShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    vm: EventHistoryViewModel,
    onBack: () -> Unit,
    onEventSelected: (eventId: String) -> Unit,
) {
    
    LaunchedEffect(Unit) {
        vm.loadEventHistory()
    }
    val eventHistoryList by vm.eventHistory.collectAsStateWithLifecycle()
    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("Are you sure you want to delete all event history? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.clearAllEvents()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (eventHistoryList.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No past events found.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(eventHistoryList, key = { it.id }) { eventItem ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    vm.deleteEvent(eventItem.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                HistoryDismissBackground(
                                    state = dismissState,
                                    shape = HistoryItemCardShape
                                )
                            },
                            content = {
                                EventHistoryItemCard(
                                    item = eventItem,
                                    onClick = { onEventSelected(eventItem.id) }
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showClearAllDialog = true },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Clear All Events",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Clear All Events")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDismissBackground(
    state: SwipeToDismissBoxState,
    shape: Shape
) {
    val isSwiping = state.targetValue != SwipeToDismissBoxValue.Settled

    val bgColor by animateColorAsState(
        targetValue = if (isSwiping) MaterialTheme.colorScheme.errorContainer
                      else MaterialTheme.colorScheme.surfaceVariant,
        label = "HistoryDismissBackgroundColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSwiping) 1.15f else 1f,
        label = "HistoryDismissIconScale"
    )

    val alignment = when (state.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.CenterEnd
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(bgColor)
            .padding(horizontal = 16.dp),
        contentAlignment = alignment
    ) {
        if (isSwiping) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete",
                modifier = Modifier.scale(iconScale),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}


@Composable
fun EventHistoryItemCard(
    item: EventHistoryItemUiState,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = HistoryItemCardShape,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.creationDateTimeFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Previews removed ---


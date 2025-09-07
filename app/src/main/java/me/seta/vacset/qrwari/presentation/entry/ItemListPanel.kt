package me.seta.vacset.qrwari.presentation.entry

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
// import androidx.compose.material.icons.filled.Edit // No longer used for item name hint
import androidx.compose.material.icons.outlined.Edit // Added for item name hint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow // Added import for TextOverflow
import androidx.compose.ui.unit.dp

private val ItemCardShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class) // Added for SwipeToDismissBoxState
@Composable
fun ItemListPanel(
    items: List<ItemUi>,
    totalAmountFormatted: String, // New parameter for the formatted total amount
    onRemove: (String) -> Unit,
    editingItemId: String?,
    itemNameEditInput: String,
    onStartEditItemName: (itemId: String) -> Unit,
    onItemNameEditInputChange: (newName: String) -> Unit,
    onConfirmEditItemName: () -> Unit,
    onCancelEditItemName: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Items",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (items.isEmpty()) {
            EmptyItemsPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Takes up available space, pushing total to the bottom of its section
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = items,
                key = { it.id }
            ) { item ->
                val isEditingCurrentItem = item.id == editingItemId

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { v ->
                        if (isEditingCurrentItem) return@rememberSwipeToDismissBoxState false
                        when (v) {
                            SwipeToDismissBoxValue.StartToEnd,
                            SwipeToDismissBoxValue.EndToStart -> {
                                onRemove(item.id)
                                true
                            }
                            SwipeToDismissBoxValue.Settled -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = !isEditingCurrentItem,
                    enableDismissFromEndToStart = !isEditingCurrentItem,
                    backgroundContent = {
                        if (!isEditingCurrentItem) {
                            DismissBackground(
                                state = dismissState,
                                shape = ItemCardShape
                            )
                        }
                    },
                    content = {
                        ElevatedCard(
                            onClick = {
                                if (!isEditingCurrentItem) {
                                    onStartEditItemName(item.id)
                                }
                            },
                            shape = ItemCardShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isEditingCurrentItem) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = itemNameEditInput,
                                        onValueChange = onItemNameEditInputChange,
                                        label = { Text("Item Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = onCancelEditItemName) {
                                            Icon(Icons.Filled.Close, contentDescription = "Cancel Edit")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = onConfirmEditItemName) {
                                            Icon(Icons.Filled.Check, contentDescription = "Confirm Edit")
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item.name.isNullOrBlank()) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Set item name",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp)) // Added spacer
                                    Text(
                                        text = item.amount,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // Display Total Amount if items are not empty
        // This will be displayed after the LazyColumn, at the bottom of the ItemListPanel's Column.
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp), // Consistent with item content padding ideas
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Total",
                style = MaterialTheme.typography.titleMedium, // Changed to titleMedium for prominence
                fontWeight = FontWeight.Bold
            )
            Text(
                totalAmountFormatted,
                style = MaterialTheme.typography.titleMedium, // Changed to titleMedium
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) 
@Composable
private fun DismissBackground(
    state: SwipeToDismissBoxState,
    shape: Shape
) {
    val isSwiping = state.targetValue != SwipeToDismissBoxValue.Settled

    val bgColor by animateColorAsState(
        targetValue = if (isSwiping) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceVariant, 
        label = "dismissBg"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSwiping) 1.15f else 1f,
        label = "dismissIconScale"
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
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                modifier = Modifier.scale(iconScale),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EmptyItemsPlaceholder(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                "No items yet.\nAdd entries from the keypad and assign to participants.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

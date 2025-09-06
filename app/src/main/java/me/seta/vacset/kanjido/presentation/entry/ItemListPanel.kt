package me.seta.vacset.kanjido.presentation.entry

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.dp

private val ItemCardShape = RoundedCornerShape(12.dp)

@Composable
fun ItemListPanel(
    items: List<ItemUi>,
    onItemClick: (ItemUi) -> Unit,
    onRemove: (String) -> Unit,
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
                .weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp) // list spacing handled here
        ) {
            items(
                items = items,
                key = { it.id }
            ) { item ->

                // Wrap each row with padding so BOTH bg and content share the same outer spacing
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp) // was on Card before; now here
                ) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { v ->
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
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = true,

                        // Background now perfectly matches the card’s bounds/shape
                        backgroundContent = {
                            DismissBackground(
                                state = dismissState,
                                shape = ItemCardShape
                            )
                        },

                        content = {
                            ElevatedCard(
                                onClick = { onItemClick(item) },
                                shape = ItemCardShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        item.amount,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

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

    // Critical bits: fill the same size AND clip to the same card shape
    Box(
        modifier = Modifier
            .fillMaxSize()      // match content size
            .clip(shape)        // prevent corner peeking
            .background(bgColor)
            .padding(horizontal = 16.dp), // same internal spacing as your content affordance
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Delete",
            modifier = Modifier.scale(iconScale),
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
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

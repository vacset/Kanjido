package me.seta.vacset.kanjido.presentation.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ItemListPanel(
    items: List<ItemUi>,
    onItemClick: (ItemUi) -> Unit,
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
        } else {
            // Replace with LazyColumn if you expect long lists
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items.forEach { item ->
                    ElevatedCard(
                        onClick = { onItemClick(item) },
                        shape = RoundedCornerShape(12.dp),
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
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
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

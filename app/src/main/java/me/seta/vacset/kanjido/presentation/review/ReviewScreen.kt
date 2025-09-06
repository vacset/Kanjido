package me.seta.vacset.kanjido.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel
import me.seta.vacset.kanjido.domain.calc.splitEvent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(
    vm: EventBuilderViewModel, onNext: () -> Unit, promptPayId: String?
) {
    val context = LocalContext.current
    val participants = vm.participants
    val items = vm.items

    // Recompute totals on every change (items/assignments/participants)
    val split = splitEvent(vm.toEvent())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding() // Added to respect system areas
            .padding(16.dp)      // Your existing padding applied after safe insets
    ) {
        Text("Assign Participants per Item", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { itemDraft ->
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text((itemDraft.label ?: "Item") + " – ฿${itemDraft.amount.setScale(2)}")
                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            participants.forEach { p ->
                                val selected = vm.selectedFor(itemDraft.id).contains(p.id)
                                FilterChip(
                                    selected = selected,
                                    onClick = { vm.toggleAssignment(itemDraft.id, p.id) },
                                    label = { Text(p.name) })
                            }
                        }

                        if (vm.selectedFor(itemDraft.id).isEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            AssistChip(
                                onClick = {},
                                label = { Text("Currently: ALL participants") })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Spacer(Modifier.height(12.dp))

        Text("Per-person totals", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        split.perPerson.forEach { pt ->
            ListItem(
                headlineContent = { Text(pt.participant.name) },
                supportingContent = { Text("฿${pt.amount.setScale(2)}") })
        }
        Spacer(Modifier.height(8.dp))
        Text("Grand total: ฿${split.grandTotal.setScale(2)}")

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (promptPayId.isNullOrBlank()) {
                    Toast.makeText(
                        context,
                        "You must set PromptPay ID in Preferences first",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    onNext()
                }
            },
            enabled = participants.isNotEmpty() && items.isNotEmpty()
        ) {
            Text("Generate QR Pages")
        }
    }
}

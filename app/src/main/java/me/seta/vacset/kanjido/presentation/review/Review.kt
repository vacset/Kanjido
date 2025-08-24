package me.seta.vacset.kanjido.presentation.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel
import me.seta.vacset.kanjido.domain.calc.splitEvent
import androidx.compose.foundation.lazy.LazyColumn

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(vm: EventBuilderViewModel, onNext: () -> Unit) {
    val participants = vm.participants
    val items = vm.items

    // Recompute every recomposition (selection, items, participants)
    val split = splitEvent(vm.toEvent())

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Assign Participants per Item", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items.size) { idx ->
                val it = items[idx]
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text((it.label ?: "Item ${idx + 1}") + "  –  ฿${it.amount.setScale(2)}")

                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            participants.forEach { p ->
                                val selected = vm.selectedFor(it.id).contains(p.id)
                                FilterChip(
                                    selected = selected,
                                    onClick = { vm.toggleAssignment(it.id, p.id) },
                                    label = { Text(p.name) }
                                )
                            }
                        }

                        if (vm.selectedFor(it.id).isEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            AssistChip(onClick = {}, label = { Text("Currently: ALL participants") })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        Text("Per-person totals", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        split.perPerson.forEach { pt ->
            ListItem(
                headlineContent = { Text(pt.participant.name) },
                supportingContent = { Text("฿${pt.amount.setScale(2)}") }
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("Grand total: ฿${split.grandTotal.setScale(2)}")

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onNext,
            enabled = participants.isNotEmpty() && items.isNotEmpty()
        ) { Text("Go to Summary") }
    }
}

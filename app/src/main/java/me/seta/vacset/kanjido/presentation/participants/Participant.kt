package me.seta.vacset.kanjido.presentation.participants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel

@Composable
fun ParticipantsScreen(vm: EventBuilderViewModel, onNext: () -> Unit) {
    var name by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Participants", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Add name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            vm.addParticipant(name)
            name = ""
        }) { Text("Add") }

        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.participants.size) { idx ->
                AssistChip(onClick = {}, label = { Text(vm.participants[idx].name) })
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onNext, enabled = vm.participants.isNotEmpty()) { Text("Next") }
    }
}

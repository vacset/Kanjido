package me.seta.vacset.kanjido.presentation.participant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ParticipantsScreen(onNext: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val people = remember { mutableStateListOf<String>() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Participants", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Add name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(onClick = { if (name.isNotBlank()) { people.add(name.trim()); name = "" } }) { Text("Add") }
        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(people.size) { idx -> AssistChip(onClick = {}, label = { Text(people[idx]) }) }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNext, enabled = people.isNotEmpty()) { Text("Next") }
    }
}

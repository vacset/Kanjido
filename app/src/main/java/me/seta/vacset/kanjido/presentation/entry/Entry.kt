package me.seta.vacset.kanjido.presentation.entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@Composable
fun EntryScreen(onNext: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<Pair<String?, BigDecimal>>() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add Items", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Amount (THB)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = {
                amountText.toBigDecimalOrNull()?.let { bd ->
                    items.add(null to bd)
                    amountText = ""
                }
            }) { Text("Add") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onNext, enabled = items.isNotEmpty()) { Text("Next") }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(items.size) { idx ->
                val (label, amt) = items[idx]
                ListItem(headlineContent = { Text(label ?: "Item ${idx+1}") }, supportingContent = { Text("฿${amt.setScale(2)}") })
                Divider()
            }
        }
    }
}

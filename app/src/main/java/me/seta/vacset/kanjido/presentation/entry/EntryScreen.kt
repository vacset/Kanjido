package me.seta.vacset.kanjido.presentation.entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun EntryScreen(
    vm: EventBuilderViewModel, onNext: () -> Unit, onQuickQr: (String) -> Unit,
    promptPayId: String?,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current

    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Add Items", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Label (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Amount (THB)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        // presentation/entry/EntryScreen.kt (only the buttons row)
        Row {
            Button(onClick = {
                amountText.toBigDecimalOrNull()?.let { bd ->
                    vm.addItem(amount = bd.setScale(2), label = label.ifBlank { null })
                    label = ""; amountText = ""
                }
            }) { Text("Add") }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onNext,
                enabled = vm.items.isNotEmpty()
            ) { Text("Next") }

            Spacer(Modifier.width(8.dp))

            // Fast QR – no participants, just one amount
            Button(
                onClick = {
                    if (promptPayId.isNullOrBlank()) {
                        Toast.makeText(
                            context,
                            "You must set PromptPay ID in Preferences first",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        amountText.toBigDecimalOrNull()?.let { bd ->
                            onQuickQr(bd.setScale(2).toPlainString())
                        }
                    }
                },
                enabled = amountText.toBigDecimalOrNull() != null
            ) {
                Text("QR Now")
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(onClick = onOpenSettings) {
                Text("Preferences")
            }
        }

        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(vm.items.size) { idx ->
                val it = vm.items[idx]
                ListItem(
                    headlineContent = { Text(it.label ?: "Item ${idx + 1}") },
                    supportingContent = { Text("฿${it.amount.setScale(2)}") }
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    }
}

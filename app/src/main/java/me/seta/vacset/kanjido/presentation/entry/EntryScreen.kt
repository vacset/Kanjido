package me.seta.vacset.kanjido.presentation.entry

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel

@Composable
fun EntryScreen(
    vm: EventBuilderViewModel,
    onNext: () -> Unit,
    onQuickQr: (String) -> Unit,
    promptPayId: String?,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    // Always side-by-side (two-pane), even on phones
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CalculatorPadPane(
            modifier = Modifier.weight(1f), // must be inside RowScope
            label = label,
            onLabelChange = { label = it },
            amountText = amountText,
            onAmountChange = { amountText = it },
            onAdd = {
                amountText.toBigDecimalOrNull()?.let { bd ->
                    vm.addItem(bd.setScale(2), label.ifBlank { null })
                    label = ""; amountText = ""
                }
            },
            onQuickQr = {
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
            onNext = onNext,
            onOpenSettings = onOpenSettings
        )

        ItemListPane(
            modifier = Modifier.weight(1f), // must be inside RowScope
            vm = vm,
            header = "Items"
        )
    }
}

@Composable
private fun CalculatorPadPane(
    modifier: Modifier = Modifier,
    label: String,
    onLabelChange: (String) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onAdd: () -> Unit,
    onQuickQr: () -> Unit,
    onNext: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
    ) {
        Text(
            "Add Item",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = label,
            onValueChange = onLabelChange,
            label = { Text("Label (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        AmountDisplay(amountText, onAmountChange)
        Spacer(Modifier.height(8.dp))

        NumberPad(
            onDigit = { d -> onAmountChange(amountText + d) },
            onDot = { if (!amountText.contains('.')) onAmountChange(amountText + ".") },
            onBackspace = { if (amountText.isNotEmpty()) onAmountChange(amountText.dropLast(1)) },
            onClear = { onAmountChange("") }
        )

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onQuickQr,
                enabled = amountText.toBigDecimalOrNull() != null
            ) { Text("QR") }
            OutlinedButton(onClick = onOpenSettings) { Text("Pref") }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onAdd,
                enabled = amountText.toBigDecimalOrNull() != null
            ) { Text("Add") }
            Button(onClick = onNext) { Text("Next") }
        }

    }
}

@Composable
private fun ItemListPane(
    modifier: Modifier = Modifier,
    vm: EventBuilderViewModel,
    header: String
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
    ) {
        Text(header, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(vm.items) { it ->
                ListItem(
                    headlineContent = { Text(it.label ?: "Item") },
                    supportingContent = { Text("฿${it.amount.setScale(2)}") }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AmountDisplay(amountText: String, onAmountChange: (String) -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("฿", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(8.dp))
            Text(
                text = amountText.ifBlank { "0" },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NumberPad(
    onDigit: (String) -> Unit,
    onDot: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    // 3×4 grid: 1 2 3 / 4 5 6 / 7 8 9 / . 0 ⌫
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { r ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                r.forEach { key ->
                    Button(
                        onClick = {
                            when (key) {
                                "⌫" -> onBackspace()
                                "." -> onDot()
                                else -> onDigit(key)
                            }
                        },
                        // IMPORTANT: use weight(1f) positionally, inside RowScope
                        modifier = Modifier.weight(1f)
                    ) { Text(key) }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedButton(
            onClick = onClear,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Clear") }
    }
}
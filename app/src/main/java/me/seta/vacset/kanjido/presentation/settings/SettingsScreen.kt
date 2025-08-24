package me.seta.vacset.kanjido.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(
    vm: PromptPayViewModel,
    onDone: () -> Unit
) {
    val currentId = vm.promptPayIdFlow.collectAsState().value ?: ""
    var localId by remember(currentId) { mutableStateOf(currentId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Organizer PromptPay ID", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = localId,
                onValueChange = { localId = it },
                label = { Text("Phone / National ID / Bank Account") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii
                ),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // TODO: add validation by calling isValidThaiNationalId before save button enabled
                Button(
                    onClick = {
                        vm.setPromptPayId(localId)
                        onDone()
                    },
                    enabled = localId.isNotBlank()
                ) { Text("Save") }
                OutlinedButton(onClick = onDone) { Text("Cancel") }
            }
            AssistChip(
                onClick = {
                    // Minimal hint only; validation is handled later by the QR builder
                },
                label = { Text("Tip: phone (08....) or citizen id only") }
            )
        }
    }
}

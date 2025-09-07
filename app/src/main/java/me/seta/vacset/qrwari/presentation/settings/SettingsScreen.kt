package me.seta.vacset.qrwari.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.seta.vacset.qrwari.domain.promptpay.detectPromptPayIdType

@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(
    vm: PromptPayViewModel,
    onDone: () -> Unit
) {
    val currentId = vm.promptPayIdFlow.collectAsState().value ?: ""
    var localId by remember(currentId) { mutableStateOf(currentId) }

    val isIdValid = remember(localId) { 
        localId.isBlank() || detectPromptPayIdType(localId) != null 
    }
    val showError = localId.isNotBlank() && !isIdValid

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
                label = { Text("Phone / National ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii
                ),
                singleLine = true,
                isError = showError,
                supportingText = {
                    Box(modifier = Modifier.heightIn(min = 48.dp)) { // Ensure consistent height
                        if (showError) {
                            Text("Invalid ID format. Use phone (e.g., 08xxxxxxxx) or 13-digit National ID.")
                        } else {
                            Text("Enter your Phone number (e.g., 08xxxxxxxx) or 13-digit National ID.")
                        }
                    }
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // The button is only enabled if isIdValid and localId is not blank
                        vm.setPromptPayId(localId)
                        onDone()
                    },
                    enabled = localId.isNotBlank() && isIdValid 
                ) { Text("Save") }
                OutlinedButton(onClick = onDone) { Text("Cancel") }
            }
        }
    }
}

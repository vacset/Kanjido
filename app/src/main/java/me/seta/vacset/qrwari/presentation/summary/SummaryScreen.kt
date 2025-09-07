package me.seta.vacset.qrwari.presentation.summary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SummaryScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Summary Sheet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Items and allocations will be listed here.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { /* export PNG */ }) { Text("Share Sheet") }
    }
}

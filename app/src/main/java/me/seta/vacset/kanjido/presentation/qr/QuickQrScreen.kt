package me.seta.vacset.kanjido.presentation.qr

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.domain.promptpay.PromptPayBuilder
import me.seta.vacset.kanjido.util.QrUtil
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import me.seta.vacset.kanjido.domain.promptpay.detectPromptPayIdType

@ExperimentalMaterial3Api
@Composable
fun QuickQrScreen(
    amountTHB: String,
    promptPayId: String?,      // Option B: saved ID passed in
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val parsedAmount = remember(amountTHB) { amountTHB.toBigDecimalOrNull()?.setScale(2) }

    // Guard: missing amount or ID -> toast + back
    if (parsedAmount == null || promptPayId.isNullOrBlank()) {
        LaunchedEffect(parsedAmount, promptPayId) {
            val msg = if (parsedAmount == null) "Amount is invalid"
            else "You must set PromptPay ID in Preferences first"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onBack()
        }
        return
    }

    // Guard: unsupported ID format -> toast + back
    val idType = remember(promptPayId) { detectPromptPayIdType(promptPayId) }
    if (idType == null) {
        LaunchedEffect(promptPayId) {
            Toast.makeText(
                context,
                "Invalid PromptPay ID. Only Phone or National ID is supported.",
                Toast.LENGTH_SHORT
            ).show()
            onBack()
        }
        return
    }

    // Build Tag 29 payload (dynamic → PoI=12)
    val payload = remember(promptPayId, parsedAmount, idType) {
        PromptPayBuilder.build(
            PromptPayBuilder.Input(
                idType = idType,
                idValueRaw = promptPayId,
                amountTHB = parsedAmount
            )
        )
    }
    val qrBitmap = remember(payload) { QrUtil.generate(payload.content, size = 512) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick QR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Amount: ฿${parsedAmount.toPlainString()}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "PromptPay QR",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(280.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Dynamic PromptPay QR (PoI=12).", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { /* TODO: share QR */ }) { Text("Share QR") }
        }
    }
}
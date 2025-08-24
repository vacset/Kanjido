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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@ExperimentalMaterial3Api
@Composable
fun QuickQrScreen(
    amountTHB: String,
    promptPayId: String?,   // <— pass from MainActivity
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val parsedAmount = remember(amountTHB) { amountTHB.toBigDecimalOrNull()?.setScale(2) }

    // Guardrails: show toast and return early if preconditions aren’t met
    if (promptPayId.isNullOrBlank() || parsedAmount == null) {
        LaunchedEffect(promptPayId, parsedAmount) {
            val msg = when {
                promptPayId.isNullOrBlank() -> "You must set PromptPay ID in Preferences first"
                else -> "Amount is invalid"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onBack()
        }
        // Render nothing while popping back
        return
    }

    // Build Tag-29 EMV payload (dynamic QR → PoI=12)
    val payload = remember(promptPayId, parsedAmount) {
        PromptPayBuilder.build(
            PromptPayBuilder.Input(
                idType = detectIdType(promptPayId),
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
            Modifier.padding(padding).fillMaxSize().padding(16.dp)
        ) {
            Text("Amount: ฿${parsedAmount.toPlainString()}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "PromptPay QR",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(280.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Dynamic PromptPay QR (PoI=12).", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { /* TODO: share QR */ }) { Text("Share QR") }
        }
    }
}

/** Same heuristic we used in the pager screen. */
private fun detectIdType(raw: String): PromptPayBuilder.IdType {
    val d = raw.filter { it.isDigit() }
    return when {
        d.startsWith("0066") || d.startsWith("66") || d.startsWith("0") -> PromptPayBuilder.IdType.PHONE
        d.length == 13 -> PromptPayBuilder.IdType.NATIONAL_ID
        else -> PromptPayBuilder.IdType.BANK_ACCOUNT
    }
}
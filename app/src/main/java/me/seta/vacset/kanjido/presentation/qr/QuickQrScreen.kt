package me.seta.vacset.kanjido.presentation.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.domain.promptpay.PromptPayBuilder
import me.seta.vacset.kanjido.util.QrUtil

@ExperimentalMaterial3Api
@Composable
fun QuickQrScreen(amountTHB: String, onClose: () -> Unit) {
    val payload = PromptPayBuilder.build(
        id = "" /* leave blank for now or fetch from Prefs if you require it */,
        amountTHB = amountTHB,
        reference = null
    )
    val qr = QrUtil.generate(payload.content, size = 512)

    Scaffold(
        topBar = { TopAppBar(title = { Text("QR") }) }
    ) { padding ->
        Column(Modifier
            .padding(padding)
            .padding(16.dp)) {
            Text("Amount: ฿$amountTHB", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Image(
                qr.asImageBitmap(),
                contentDescription = "PromptPay QR",
                modifier = Modifier.size(240.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row {
                Button(onClick = onClose) { Text("Close") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { /* share PNG later */ }) { Text("Share QR") }
            }
        }
    }
}

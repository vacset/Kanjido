package me.seta.vacset.kanjido.presentation.review

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.domain.promptpay.PromptPayBuilder
import me.seta.vacset.kanjido.util.QrUtil

@Composable
fun ReviewScreen(onNext: () -> Unit) {
    // Stub: imagine we have totals by participant and a promptpay id
    val ppId = "0812345678"
    val qr = QrUtil.generate(PromptPayBuilder.build(ppId, "210.00", "KANJIDO").content, 512)

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Review & Generate", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Image(bitmap = qr.asImageBitmap(), contentDescription = "PromptPay QR", modifier = Modifier.size(240.dp))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNext) { Text("Go to Summary") }
    }
}

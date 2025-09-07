package me.seta.vacset.qrwari.presentation.qr

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import me.seta.vacset.qrwari.domain.promptpay.PromptPayBuilder
import me.seta.vacset.qrwari.util.QrUtil
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share // Added import for Share icon
import me.seta.vacset.qrwari.domain.promptpay.detectPromptPayIdType
import me.seta.vacset.qrwari.util.ShareUtil

@ExperimentalMaterial3Api
@Composable
fun QuickQrScreen(
    amountTHB: String,
    promptPayId: String?,      // Option B: saved ID passed in
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val parsedAmount = remember(amountTHB) { amountTHB.toBigDecimalOrNull()?.setScale(2) }

    var showRemarkDialog by remember { mutableStateOf(false) }
    var remarkInputValue by remember { mutableStateOf("") }

    if (parsedAmount == null || promptPayId.isNullOrBlank()) {
        LaunchedEffect(parsedAmount, promptPayId) {
            val msg = if (parsedAmount == null) "Amount is invalid"
            else "You must set PromptPay ID in Preferences first"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onBack()
        }
        return
    }

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
    val defaultScreenTitle = "Quick QR"
    val amountTextForSharing = "Amount: ฿${parsedAmount.toPlainString()}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(defaultScreenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(amountTextForSharing, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "PromptPay QR",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp) 
                    .height(280.dp) 
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                showRemarkDialog = true
            }) { 
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Share Page") 
            }
        }
    }

    if (showRemarkDialog) {
        AlertDialog(
            onDismissRequest = {
                showRemarkDialog = false
                remarkInputValue = "" // Clear input on dismiss
            },
            title = { Text("Any note to payer?") },
            text = {
                OutlinedTextField(
                    value = remarkInputValue,
                    onValueChange = { remarkInputValue = it },
                    label = { Text("Remark (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemarkDialog = false
                        val titleForSharing = remarkInputValue.trim().ifBlank { defaultScreenTitle }
                        val compositeBitmap = ShareUtil.createShareableImage(
                            context = context,
                            title = titleForSharing,
                            subtitle = amountTextForSharing,
                            qrBitmap = qrBitmap
                        )
                        ShareUtil.shareBitmap(
                            context = context,
                            bitmap = compositeBitmap,
                            fileName = "quick_qr_page_with_remark.png"
                        )
                        remarkInputValue = "" // Clear input after sharing
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRemarkDialog = false
                        remarkInputValue = "" // Clear input on cancel
                    }
                ) { Text("Cancel") }
            }
        )
    }
}

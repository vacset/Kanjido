package me.seta.vacset.qrwari.presentation.review

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share // Added import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.seta.vacset.qrwari.domain.calc.splitEvent
import me.seta.vacset.qrwari.domain.promptpay.PromptPayBuilder
import me.seta.vacset.qrwari.domain.promptpay.detectPromptPayIdType
import me.seta.vacset.qrwari.presentation.state.EventBuilderViewModel
import me.seta.vacset.qrwari.util.QrUtil
import me.seta.vacset.qrwari.util.ShareUtil

@ExperimentalMaterial3Api
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QrPagerScreen(
    vm: EventBuilderViewModel,
    promptPayId: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    if (promptPayId.isNullOrBlank()) {
        LaunchedEffect(promptPayId) {
            Toast.makeText(context, "You must set PromptPay ID in Preferences first", Toast.LENGTH_SHORT).show()
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

    val event = vm.toEvent()
    val split = splitEvent(event)
    val pages = split.perPerson.size + 1

    val pagerState = rememberPagerState(pageCount = { pages })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.name) },
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
                .padding(16.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                if (page < split.perPerson.size) {
                    val card = split.perPerson[page]
                    val participantName = card.participant.name
                    val amountStr = "Amount: ฿${card.amount.setScale(2).toPlainString()}"

                    val payload = remember(promptPayId, card.amount) {
                        PromptPayBuilder.build(
                            PromptPayBuilder.Input(
                                idType = idType,
                                idValueRaw = promptPayId,
                                amountTHB = card.amount.setScale(2)
                            )
                        )
                    }
                    val qrBitmap = remember(payload) { QrUtil.generate(payload.content, size = 512) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.Center, // Added for vertical centering
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(participantName, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(amountStr, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "PromptPay QR for $participantName",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(280.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            val compositeBitmap = ShareUtil.createShareableImage(
                                context = context,
                                title = participantName,
                                subtitle = amountStr,
                                qrBitmap = qrBitmap
                            )
                            ShareUtil.shareBitmap(
                                context = context,
                                bitmap = compositeBitmap,
                                fileName = "${event.name}_${participantName.replace(" ", "_")}_qr_page.png"
                            )
                        }) { 
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share Page",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Share Page") 
                        }
                    }
                } else {
                    // Summary page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                        // Consider centering this content as well if desired
                        // verticalArrangement = Arrangement.Center,
                        // horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Summary", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                        Spacer(Modifier.height(8.dp))
                        split.perPerson.forEach { pt ->
                            ListItem(
                                headlineContent = { Text(pt.participant.name) },
                                supportingContent = { Text("฿${pt.amount.setScale(2)}") }
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            Toast.makeText(context, "Sharing summary as an image is not yet implemented.", Toast.LENGTH_LONG).show()
                        }) { Text("Share Sheet") }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                    },
                    enabled = pagerState.currentPage > 0
                ) { Text("< Prev") }

                TextButton(
                    onClick = {
                        if (pagerState.currentPage < pages - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    enabled = pagerState.currentPage < pages - 1
                ) { Text("Next >") }
            }
        }
    }
}

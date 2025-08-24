package me.seta.vacset.kanjido.presentation.review

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.seta.vacset.kanjido.domain.calc.splitEvent
import me.seta.vacset.kanjido.domain.promptpay.PromptPayBuilder
import me.seta.vacset.kanjido.domain.promptpay.detectPromptPayIdType
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel
import me.seta.vacset.kanjido.util.QrUtil

@ExperimentalMaterial3Api
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QrPagerScreen(
    vm: EventBuilderViewModel,
    promptPayId: String?,      // Option B: saved ID passed in
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Guard: missing ID -> toast + back
    if (promptPayId.isNullOrBlank()) {
        LaunchedEffect(promptPayId) {
            Toast.makeText(context, "You must set PromptPay ID in Preferences first", Toast.LENGTH_SHORT).show()
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

    val event = vm.toEvent()              // Event no longer carries the ID
    val split = splitEvent(event)
    val pages = split.perPerson.size + 1  // people pages + 1 summary

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
                    val amount = card.amount.setScale(2).toPlainString()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(card.participant.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("Amount: ฿$amount", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))

                        // Build Tag 29 (dynamic, PoI=12)
                        val payload = remember(promptPayId, amount) {
                            PromptPayBuilder.build(
                                PromptPayBuilder.Input(
                                    idType = idType,
                                    idValueRaw = promptPayId,
                                    amountTHB = card.amount.setScale(2)
                                )
                            )
                        }
                        val qr = remember(payload) { QrUtil.generate(payload.content, size = 512) }

                        Image(
                            bitmap = qr.asImageBitmap(),
                            contentDescription = "PromptPay QR",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(280.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Dynamic PromptPay QR (PoI=12).", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { /* TODO: share this QR bitmap */ }) { Text("Share QR") }
                    }
                } else {
                    // Summary page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
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
                        Button(onClick = { /* TODO: share sheet */ }) { Text("Share Sheet") }
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
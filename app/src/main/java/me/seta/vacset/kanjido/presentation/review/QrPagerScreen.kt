package me.seta.vacset.kanjido.presentation.review

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.seta.vacset.kanjido.domain.calc.splitEvent
import me.seta.vacset.kanjido.domain.promptpay.PromptPayBuilder
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel
import me.seta.vacset.kanjido.util.QrUtil
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment

@ExperimentalMaterial3Api
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QrPagerScreen(
    vm: EventBuilderViewModel,
    promptPayId: String?,            // Option B: pass saved ID directly
    onBack: () -> Unit
) {
    val event = vm.toEvent()         // Event no longer needs promptPayId
    val split = splitEvent(event)
    val pages = split.perPerson.size + 1 // people pages + 1 summary

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
                    // --------- Per-person page (VERTICAL layout) ---------
                    val card = split.perPerson[page]
                    val amount = card.amount.setScale(2).toPlainString()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = card.participant.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Amount: ฿$amount",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(16.dp))

                        if (!promptPayId.isNullOrBlank()) {
                            val payload = PromptPayBuilder.build(
                                PromptPayBuilder.Input(
                                    idType = detectIdType(promptPayId),
                                    idValueRaw = promptPayId,
                                    amountTHB = card.amount.setScale(2) // dynamic QR (PoI=12)
                                )
                            )
                            val qr = QrUtil.generate(payload.content, size = 512)

                            Image(
                                bitmap = qr.asImageBitmap(),
                                contentDescription = "PromptPay QR",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .height(280.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Dynamic PromptPay QR (PoI=12).",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { /* TODO: share this QR bitmap */ }) {
                                Text("Share QR")
                            }
                        } else {
                            Text(
                                "PromptPay ID not set. Open Preferences to configure it.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // -------------------- Summary page --------------------
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        Text("Summary", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))
                        split.perPerson.forEach { pt ->
                            ListItem(
                                headlineContent = { Text(pt.participant.name) },
                                supportingContent = { Text("฿${pt.amount.setScale(2)}") }
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { /* TODO: share summary */ }) { Text("Share Sheet") }
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
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    enabled = pagerState.currentPage > 0
                ) { Text("< Prev") }

                TextButton(
                    onClick = {
                        if (pagerState.currentPage < pages - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    enabled = pagerState.currentPage < pages - 1
                ) { Text("Next >") }
            }
        }
    }
}

/**
 * Heuristic to guess PromptPay ID type.
 * - 0066..., +66..., 66..., or local 0xxxxxxxxx → PHONE
 * - 13 digits → NATIONAL_ID
 * - otherwise → BANK_ACCOUNT
 */
private fun detectIdType(raw: String): PromptPayBuilder.IdType {
    val d = raw.filter { it.isDigit() }
    return when {
        d.startsWith("0066") || d.startsWith("66") || d.startsWith("0") ->
            PromptPayBuilder.IdType.PHONE
        d.length == 13 ->
            PromptPayBuilder.IdType.NATIONAL_ID
        else ->
            PromptPayBuilder.IdType.BANK_ACCOUNT
    }
}
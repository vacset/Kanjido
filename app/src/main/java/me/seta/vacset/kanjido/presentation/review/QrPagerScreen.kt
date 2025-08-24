package me.seta.vacset.kanjido.presentation.review

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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

@ExperimentalMaterial3Api
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QrPagerScreen(
    vm: EventBuilderViewModel,
    onClose: () -> Unit
) {
    val event = vm.toEvent()
    val split = splitEvent(event)
    val pages = split.perPerson.size + 1 // last page = summary

    val pagerState = rememberPagerState(pageCount = { pages })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = event.name) })
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
                    // Per-person QR page
                    val personTotal = split.perPerson[page]
                    val amount = personTotal.amount.setScale(2).toPlainString()

                    val payload = PromptPayBuilder.build(
                        id = event.promptPayId.orEmpty(),
                        amountTHB = amount,
                        reference = null
                    )
                    val qr = QrUtil.generate(payload.content, size = 512)

                    Text(
                        text = personTotal.participant.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Amount: ฿$amount",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Image(
                        bitmap = qr.asImageBitmap(),
                        contentDescription = "PromptPay QR",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(280.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { /* TODO: share this QR bitmap */ }) {
                        Text("Share QR")
                    }
                } else {
                    // Summary page
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    Spacer(Modifier.height(8.dp))
                    split.perPerson.forEach { pt ->
                        ListItem(
                            headlineContent = { Text(pt.participant.name) },
                            supportingContent = { Text("฿${pt.amount.setScale(2)}") }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { /* TODO: share summary */ }) {
                        Text("Share Sheet")
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

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onClose) { Text("Close") }
        }
    }
}

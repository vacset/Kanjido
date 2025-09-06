package me.seta.vacset.kanjido.presentation.entry

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.seta.vacset.kanjido.presentation.state.EventBuilderViewModel

/**
 * Entry screen layout:
 * - Top bar: Event name (left) + edit icon, history icon; settings icon on the right.
 * - Participants row beneath top bar: chips + add chip.
 * - Body is a Row (always side-by-side on phones too):
 *    Left pane: Amount display, numeric pad, "Capture bill" button
 *    Right pane: Item list, "Quick QR" button
 *
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EntryScreen(
    vm: EventBuilderViewModel,
    promptPayId: String?,
    // ----- Navigation / actions -----
    onEditEventName: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (ParticipantUi) -> Unit,
    onPadPress: (PadKey) -> Unit,
    onCaptureBill: () -> Unit,
    onQuickQr: (String) -> Unit,
    onItemClick: (ItemUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val participantsDomain = vm.participants
    // Map domain → UI models expected by EntryScreen
    val participantsUi = participantsDomain.map { p ->
        ParticipantUi(
            id = p.id,
            name = p.name
        )
    }
    val itemsDraft = vm.items
    val amountText = vm.amountText

    // Map domain → UI models expected by EntryScreen
    val itemsUi = itemsDraft.map { d ->
        ItemUi(
            id = d.id,
            name = d.label ?: "Item",
            amount = d.amount.toPlainString()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left: Event name + edit + history
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = vm.eventName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = onEditEventName) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit event")
                            }
                            IconButton(onClick = onOpenHistory) {
                                Icon(Icons.Default.History, contentDescription = "Event history")
                            }
                        }

                        // Right: Settings
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Participants panel
            ParticipantPanel(
                participants = vm.participants,
                isAdding = vm.isAddingParticipant,
                input = vm.participantInput,
                suggestions = vm.participantSuggestions,
                onStartAdd = vm::startAddParticipant,
                onCancelAdd = vm::cancelAddParticipant,
                onInputChange = vm::updateParticipantInput,
                onConfirmAdd = vm::confirmAddParticipant,
                onPickSuggestion = vm::pickSuggestion,
                onRemoveById = vm::removeParticipantById,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Two-pane layout
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LEFT PANE
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AmountDisplay(
                        amountText = amountText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 72.dp)
                    )

                    NumericPad(
                        onPress = onPadPress,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onCaptureBill,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Capture bill")
                    }
                }

                // RIGHT PANE
                Column(
                    modifier = Modifier
                        .weight(0.48f)
                        .fillMaxHeight()
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ItemListPanel(
                        items = itemsUi,
                        onItemClick = onItemClick,
                        onRemove = { id -> vm.removeItemById(id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    FilledTonalButton(
                        onClick = {
                            if (promptPayId.isNullOrBlank()) {
                                Toast.makeText(
                                    context,
                                    "You must set PromptPay ID in Preferences first",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                amountText.toBigDecimalOrNull()?.let { bd ->
                                    onQuickQr(bd.setScale(2).toPlainString())
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Quick QR")
                    }
                }
            }
        }
    }
}

/* -------------------------- UI MODELS (sample) -------------------------- */

data class ParticipantUi(
    val id: String,
    val name: String,
    val colorSeed: Int? = null, // if you color-code chips
)

data class ItemUi(
    val id: String,
    val name: String,
    val amount: String,
    val byParticipantIds: List<String> = emptyList()
)

/* -------------------------- PARTICIPANTS PANEL -------------------------- */

@Composable
private fun ParticipantPanel(
    participants: List<ParticipantUi>,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (ParticipantUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Participants",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(2.dp))
            participants.forEach { p ->
                AssistChip(
                    onClick = { /* could select */ },
                    label = { Text(p.name) },
                    modifier = Modifier.padding(end = 8.dp),
                    leadingIcon = null,
                    trailingIcon = {
                        Text(
                            "×",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onRemoveParticipant(p) }
                                .padding(horizontal = 6.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
            InputChip(
                selected = false,
                onClick = onAddParticipant,
                label = { Text("Add") },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

/* -------------------------- AMOUNT DISPLAY -------------------------- */

@Composable
private fun AmountDisplay(
    amountText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = amountText.ifBlank { "0" },
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

/* -------------------------- NUMERIC PAD -------------------------- */

sealed interface PadKey {
    data class Digit(val d: Int) : PadKey
    data object Dot : PadKey
    data object Backspace : PadKey
    data object Clear : PadKey
    data object Enter : PadKey
}

@Composable
private fun KeyRow(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
private fun RowScope.KeyCell(content: @Composable () -> Unit) {
    Box(modifier = Modifier.weight(1f)) { content() }
}


@Composable
private fun NumericPad(
    onPress: (PadKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        KeyRow {
            KeyCell { KeyButton(label = "7") { onPress(PadKey.Digit(7)) } }
            KeyCell { KeyButton(label = "8") { onPress(PadKey.Digit(8)) } }
            KeyCell { KeyButton(label = "9") { onPress(PadKey.Digit(9)) } }
        }
        KeyRow {
            KeyCell { KeyButton(label = "4") { onPress(PadKey.Digit(4)) } }
            KeyCell { KeyButton(label = "5") { onPress(PadKey.Digit(5)) } }
            KeyCell { KeyButton(label = "6") { onPress(PadKey.Digit(6)) } }
        }
        KeyRow {
            KeyCell { KeyButton(label = "1") { onPress(PadKey.Digit(1)) } }
            KeyCell { KeyButton(label = "2") { onPress(PadKey.Digit(2)) } }
            KeyCell { KeyButton(label = "3") { onPress(PadKey.Digit(3)) } }
        }
        KeyRow {
            KeyCell { KeyButton(label = "C") { onPress(PadKey.Clear) } }
            KeyCell { KeyButton(label = "0") { onPress(PadKey.Digit(0)) } }
            KeyCell { KeyButton(label = ".") { onPress(PadKey.Dot) } }
        }
        KeyRow {
            KeyCell { KeyButton(icon = Icons.Default.Backspace,
                iconContentDescription = "Backspace") { onPress(PadKey.Backspace) } }
            KeyCell {
                KeyButton(
                    icon = Icons.Default.LibraryAdd,
                    iconContentDescription = "Add item",
                    filled = true
                ) { onPress(PadKey.Enter) }
            }
            KeyCell { Spacer(modifier = Modifier.height(1.dp)) }
        }
    }
}

@Composable
private fun KeyButton(
    label: String? = null,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    filled: Boolean = false,
    onClick: () -> Unit
) {
    require(label != null || icon != null) { "KeyButton must have a label or an icon." }

    val buttonContent: @Composable () -> Unit = {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(label!!) // label is non-null if icon is null due to require
        }
    }
    val customContentPadding = PaddingValues(12.dp)
    if (filled) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
            contentPadding = customContentPadding
        ) {
            buttonContent()
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
            contentPadding = customContentPadding
        ) {
            buttonContent()
        }
    }
}


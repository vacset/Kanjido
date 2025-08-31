package me.seta.vacset.kanjido.presentation.entry

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import me.seta.vacset.kanjido.data.model.Participant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParticipantPanel(
    participants: List<Participant>,
    isAdding: Boolean,
    input: String,
    suggestions: List<String>,
    onStartAdd: () -> Unit,
    onCancelAdd: () -> Unit,
    onInputChange: (String) -> Unit,
    onConfirmAdd: () -> Unit,
    onPickSuggestion: (String) -> Unit,
    onRemoveById: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Participants",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            participants.forEach { p ->
                ParticipantChip(
                    label = p.name,
                    onRemove = { onRemoveById(p.id) }
                )
            }

            if (!isAdding) {
                AddChip(onClick = onStartAdd)
            } else {
                AddParticipantInputChip(
                    value = input,
                    onValueChange = onInputChange,
                    onConfirm = onConfirmAdd,
                    onCancel = onCancelAdd,
                    suggestions = suggestions,
                    onPickSuggestion = onPickSuggestion
                )
            }
        }
    }
}

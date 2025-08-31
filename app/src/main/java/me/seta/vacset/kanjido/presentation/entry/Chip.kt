package me.seta.vacset.kanjido.presentation.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

object ChipDefaults {
    val Height = 32.dp
    val Corner = 24.dp
    val HPadding = 12.dp
    val IconSize = 18.dp
    val Spacing = 4.dp
    val Elevation = 1.dp
    val Shape = RoundedCornerShape(Corner)

    @Composable
    fun border(): BorderStroke? = ButtonDefaults.outlinedButtonBorder
    val TextStyle: TextStyle @Composable get() = MaterialTheme.typography.bodyMedium
}

/** One surface to rule them all. Turns off 48dp enforcement and fixes height. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipContainer(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        val border = ButtonDefaults.outlinedButtonBorder
        val baseModifier = Modifier
            .height(ChipDefaults.Height)
            .then(modifier)

        val rowContent: @Composable () -> Unit = {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = ChipDefaults.HPadding),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }

        if (onClick != null) {
            Surface(
                onClick = onClick,
                shape = ChipDefaults.Shape,
                tonalElevation = ChipDefaults.Elevation,
                border = border,
                enabled = enabled,
                modifier = baseModifier
            ) { rowContent() }
        } else {
            Surface(
                shape = ChipDefaults.Shape,
                tonalElevation = ChipDefaults.Elevation,
                border = border,
                modifier = baseModifier
            ) { rowContent() }
        }
    }
}


/** Compact icon with ripple; avoids IconButton’s 48dp min size. */
@Composable
fun ActionIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier
            .size(ChipDefaults.IconSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick
            )
    )
}

/** [name|-] */
@Composable
fun ParticipantChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    ChipContainer(modifier = modifier) {
        Text(
            text = label,
            style = ChipDefaults.TextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(ChipDefaults.Spacing))
        ActionIcon(
            imageVector = Icons.Outlined.Remove,
            contentDescription = "Remove $label",
            onClick = onRemove
        )
    }
}

/** Add chip with [+] and label */
@Composable
fun AddChip(
    text: String = "Add",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ChipContainer(onClick = onClick, modifier = modifier) {
        Text(text, style = ChipDefaults.TextStyle)
        Spacer(Modifier.width(ChipDefaults.Spacing))
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add",
            modifier = Modifier.size(ChipDefaults.IconSize)
        )
    }
}

/** Input chip that matches height/shape; uses BasicTextField to avoid TextField min-height. */
@Composable
fun AddParticipantInputChip(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    suggestions: List<String>,
    onPickSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // We anchor the DropdownMenu to this Box (the chip’s bounds)
    var anchorWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(
        modifier = modifier.onSizeChanged { anchorWidthPx = it.width }
    ) {
        // The chip itself (same height/shape as others)
        ChipContainer {
            val textStyle = ChipDefaults.TextStyle

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    // Keep baseline centered within chip height
                    Box(
                        modifier = Modifier
                            .height(ChipDefaults.Height)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Enter name",
                                style = textStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(Modifier.width(ChipDefaults.Spacing))

            ActionIcon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Confirm",
                onClick = onConfirm
            )
            Spacer(Modifier.width(ChipDefaults.Spacing))
            ActionIcon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = "Cancel",
                onClick = onCancel
            )
        }

        // Suggestions dropdown anchored to the chip width
        // If you want the menu to close immediately after a pick, clear the suggestions list in your VM inside pickSuggestion(...) or right after onPickSuggestion is called.
        DropdownMenu(
            expanded = suggestions.isNotEmpty(),
            onDismissRequest = { /* keep visible while typing */ },
            modifier = Modifier.width(with(density) { anchorWidthPx.toDp() }),
            properties = PopupProperties(focusable = false)
        ) {
            suggestions.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onPickSuggestion(name) }
                )
            }
        }
    }
}
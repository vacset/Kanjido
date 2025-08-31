package me.seta.vacset.kanjido.presentation.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import me.seta.vacset.kanjido.data.model.Event
import me.seta.vacset.kanjido.data.model.Item
import me.seta.vacset.kanjido.data.model.Participant
import me.seta.vacset.kanjido.util.generateEventName
import java.math.BigDecimal
import java.util.UUID
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class EventBuilderViewModel : ViewModel() {

    // Participants
    val participants = mutableStateListOf<Participant>()

    fun addParticipant(name: String) {
        if (name.isBlank()) return
        participants.add(Participant(name = name.trim()))
    }

    fun removeParticipant(id: String) {
        participants.removeAll { it.id == id }
    }

    // Items (as entered from Entry screen)
    val items = mutableStateListOf<ItemDraft>()

    // Calculated total amount of all items
    val totalAmount: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, itemDraft -> acc.add(itemDraft.amount) }

    fun addItem(amount: BigDecimal, label: String?) {
        items.add(
            ItemDraft(
                label = label?.trim().takeUnless { it.isNullOrBlank() },
                amount = amount
            )
        )
        // TODO: add to history once the first item is entered
    }

    // Build a domain Event with actual Item objects + selected tags
    fun toEvent(name: String = generateEventName()): Event {
        val ps = participants.toList()
        val idSetAll = ps.map { it.id }.toSet()

        val builtItems = items.map { draft ->
            val selected = selectedByItemId[draft.id]
            Item(
                id = draft.id,
                label = draft.label,
                amount = draft.amount,
                taggedParticipantIds = when {
                    selected == null || selected.isEmpty() -> setOf("ALL") // default
                    selected.containsAll(idSetAll) -> setOf("ALL")
                    else -> selected
                }
            )
        }
        return Event(
            name = name,
            participants = ps,
            items = builtItems
        )
    }

    // Per-item selection map: itemId -> selected participant IDs
    private val selectedByItemId = mutableStateMapOf<String, MutableSet<String>>()

    fun toggleAssignment(itemId: String, participantId: String) {
        val set = selectedByItemId.getOrPut(itemId) { mutableSetOf() }
        if (!set.add(participantId)) set.remove(participantId)
    }

    fun selectedFor(itemId: String): Set<String> = selectedByItemId[itemId] ?: emptySet()

    // Draft representation for UI before we build domain Items
    data class ItemDraft(
        val id: String = UUID.randomUUID().toString(),
        val label: String? = null,
        val amount: BigDecimal
    )

    // Event name shown on EntryScreen (editable)
    var eventName by mutableStateOf(generateEventName())
        private set

    fun updateEventName(name: String) {
        eventName = name.ifBlank { generateEventName() }
    }

    // Numeric input currently on the left pane of EntryScreen
    var amountText by mutableStateOf("0")
        private set

    fun clearAmount() {
        amountText = "0"
    }

    fun backspace() {
        amountText = amountText.dropLast(1).ifBlank { "0" }
    }

    fun pressDot() {
        if (!amountText.contains('.')) amountText = amountText + "."
    }

    fun pressDigit(d: Int) {
        if (d !in 0..9) return
        amountText = if (amountText == "0") d.toString() else amountText + d.toString()
    }

    /**
     * Confirm current amount input. You can extend this to immediately create an item.
     * For now it only normalizes the text (no-op). */
    fun enterAmount() {
        // Keep as hook; if you want to auto-add: addItem(BigDecimal(normalized), label = null)
        amountText = amountText.trim().ifBlank { "0" }
        // no-op if 0
        val number = BigDecimal(amountText)
        if (number.compareTo(BigDecimal.ZERO) == 0) {
            return
        }
        addItem(number, label = null)
        clearAmount()
    }
}

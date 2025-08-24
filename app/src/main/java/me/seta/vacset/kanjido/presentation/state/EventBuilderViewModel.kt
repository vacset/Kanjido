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

class EventBuilderViewModel : ViewModel() {

    // Participants
    val participants = mutableStateListOf<Participant>()

    fun addParticipant(name: String) {
        if (name.isBlank()) return
        participants.add(Participant(name = name.trim()))
    }

    // Items (as entered from Entry screen)
    val items = mutableStateListOf<ItemDraft>()

    fun addItem(amount: BigDecimal, label: String?) {
        items.add(ItemDraft(label = label?.trim().takeUnless { it.isNullOrBlank() }, amount = amount))
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
}

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

    // --- Participant Panel UI state & autosuggest ---
    var isAddingParticipant by mutableStateOf(false)
        private set

    var participantInput by mutableStateOf("")
        private set

    var participantSuggestions by mutableStateOf(listOf<String>())
        private set

    // In-VM name stats (swap to DataStore/Room later if needed)
    private val nameFreq = mutableMapOf<String, Int>()
    private val coOccur = mutableMapOf<String, MutableMap<String, Int>>() // A -> (B -> weight)

    // === Intents for the UI ===
    fun startAddParticipant() {
        isAddingParticipant = true
        participantInput = ""
        refreshParticipantSuggestions()
    }

    fun cancelAddParticipant() {
        isAddingParticipant = false
        participantInput = ""
        participantSuggestions = emptyList()
    }

    fun updateParticipantInput(text: String) {
        participantInput = text
        refreshParticipantSuggestions()
    }

    fun confirmAddParticipant() {
        val name = participantInput.trim()
        if (name.isEmpty()) return
        // prevent duplicate names (case-insensitive)
        val exists = participants.any { it.name.equals(name, ignoreCase = true) }
        if (exists) {
            // You may surface a Snackbar in UI; here we just close the editor.
            participantInput = ""
            isAddingParticipant = false
            participantSuggestions = emptyList()
            return
        }
        addParticipant(name) // reuses your existing method
        // Record stats using the current set of participant names
        bumpFreq(name)
        recordCoOccurrence(name, participants.map { it.name }.toSet())
        participantInput = ""
        isAddingParticipant = false
        refreshParticipantSuggestions()
    }

    fun removeParticipantById(id: String) {
        // convenience wrapper for UI; delegates to your existing removeParticipant(id)
        removeParticipant(id)
        refreshParticipantSuggestions()
    }

    fun pickSuggestion(name: String) {
        participantInput = name
        // keep explicit confirmation UX
        // confirmAddParticipant() // enable if you prefer auto-confirm
    }

    // === Suggestion logic (co-occurrence + small base frequency blend) ===
    private fun refreshParticipantSuggestions() {
        val input = participantInput.trim()
        val presentNames = participants.map { it.name }.toSet()
        val exclude = presentNames + input

        val base = suggestFor(presentNames, limit = 20, exclude = exclude)
        participantSuggestions = if (input.isBlank()) {
            base.take(8)
        } else {
            base.filter { it.contains(input, ignoreCase = true) }.take(8)
        }
    }

    private fun bumpFreq(name: String) {
        nameFreq[name] = (nameFreq[name] ?: 0) + 1
    }

    private fun recordCoOccurrence(newName: String, presentWith: Set<String>) {
        presentWith.forEach { other ->
            if (other.equals(newName, ignoreCase = true)) return@forEach
            coOccur.getOrPut(other) { mutableMapOf() }.merge(newName, 1, Int::plus)
            coOccur.getOrPut(newName) { mutableMapOf() }.merge(other, 1, Int::plus)
        }
    }

    private fun topFrequent(limit: Int, exclude: Set<String>): List<String> =
        nameFreq.entries
            .asSequence()
            .filter { it.key !in exclude }
            .sortedByDescending { it.value }
            .map { it.key }
            .take(limit)
            .toList()

    private fun suggestFor(
        present: Set<String>,
        limit: Int,
        exclude: Set<String>
    ): List<String> {
        if (present.isEmpty()) return topFrequent(limit, exclude)
        val scores = mutableMapOf<String, Double>()
        // co-occurrence driven
        present.forEach { p ->
            coOccur[p]?.forEach { (candidate, w) ->
                if (candidate !in exclude && candidate !in present) {
                    scores[candidate] = (scores[candidate] ?: 0.0) + w
                }
            }
        }
        // small base-freq blend
        nameFreq.forEach { (n, f) ->
            if (n !in exclude && n !in present) {
                scores[n] = (scores[n] ?: 0.0) + f * 0.15
            }
        }
        return scores.entries.sortedByDescending { it.value }.map { it.key }.take(limit)
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

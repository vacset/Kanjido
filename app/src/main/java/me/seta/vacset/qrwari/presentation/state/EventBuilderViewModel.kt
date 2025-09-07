package me.seta.vacset.qrwari.presentation.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.seta.vacset.qrwari.data.model.Event
import me.seta.vacset.qrwari.data.model.Item
import me.seta.vacset.qrwari.data.model.Participant
// Import for EventWithDetails
import me.seta.vacset.qrwari.data.storage.EventWithDetails 
import me.seta.vacset.qrwari.data.repository.EventHistoryRepository
import me.seta.vacset.qrwari.util.generateEventName
import java.math.BigDecimal
import java.util.UUID
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

class EventBuilderViewModel(
    private val eventHistoryRepository: EventHistoryRepository
) : ViewModel() {

    private var currentEventDatabaseId: String? = null
    // Participants
    val participants = mutableStateListOf<Participant>()
    // Bill Images
    val billImageUris = mutableStateListOf<String>()


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
        viewModelScope.launch { attemptToSaveOrUpdateEvent() }
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
        viewModelScope.launch { attemptToSaveOrUpdateEvent() }
    }

    fun removeItemById(itemId: String) {
        items.removeAll { it.id == itemId }
        selectedByItemId.remove(itemId)
        if (itemId == editingItemId) {
            cancelEditItemName()
        }
        // If removing an item causes the event to no longer meet save criteria,
        // and it was previously saved, it will still exist in the DB.
        // Subsequent valid changes will update it.
        // viewModelScope.launch { attemptToSaveOrUpdateEvent() } // Optional: re-evaluate save
    }

    fun toEvent(id: String? = null, name: String = eventName): Event {
        val ps = participants.toList()
        val idSetAll = ps.map { it.id }.toSet()
        val builtItems = items.map { draft ->
            val selected = selectedByItemId[draft.id]
            Item(
                id = draft.id,
                label = draft.label,
                amount = draft.amount,
                taggedParticipantIds = when {
                    selected == null || selected.isEmpty() -> setOf("ALL")
                    selected.containsAll(idSetAll) -> setOf("ALL")
                    else -> selected
                }
            )
        }
        return Event(
            id = id ?: currentEventDatabaseId ?: UUID.randomUUID().toString(),
            name = name.ifBlank { generateEventName() }, // Ensure name is not blank
            participants = ps,
            items = builtItems,
            billImageUris = this.billImageUris.toList() // Add bill image URIs
        )
    }

    private val selectedByItemId = mutableStateMapOf<String, MutableSet<String>>()

    fun toggleAssignment(itemId: String, participantId: String) {
        val currentSelections = selectedByItemId[itemId]?.toMutableSet() ?: mutableSetOf()
        if (!currentSelections.add(participantId)) {
            currentSelections.remove(participantId)
        }
        if (currentSelections.isEmpty()) {
            selectedByItemId.remove(itemId)
        } else {
            selectedByItemId[itemId] = currentSelections
        }
    }

    fun assignToAll(itemId: String) {
        selectedByItemId.remove(itemId)
    }

    fun selectedFor(itemId: String): Set<String> = selectedByItemId[itemId] ?: emptySet()

    data class ItemDraft(
        val id: String = UUID.randomUUID().toString(),
        val label: String? = null,
        val amount: BigDecimal
    )

    var eventName by mutableStateOf(generateEventName())
        private set

    fun updateEventName(name: String) {
        eventName = name.ifBlank { generateEventName() }
        viewModelScope.launch { attemptToSaveOrUpdateEvent() }
    }

    var amountText by mutableStateOf("0")
        private set

    fun clearAmount() {
        amountText = "0"
    }

    fun negateAmount() {
        if (amountText == "0" || amountText.isBlank()) return
        amountText = if (amountText.startsWith("-")) {
            amountText.substring(1)
        } else {
            "-$amountText"
        }
    }

    fun backspace() {
        amountText = amountText.dropLast(1).ifBlank { "0" }
    }

    fun pressDot() {
        if (!amountText.contains('.')) amountText = "$amountText."
    }

    fun pressDigit(d: Int) {
        if (d !in 0..9) return
        val currentNumber = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (currentNumber.compareTo(BigDecimal.ZERO) == 0 && !amountText.contains(".")) {
             // If current is 0 (and not "0."), replace with digit, unless it's a negative sign only
            amountText = if (amountText == "-") "-$d" else d.toString()        
        } else {
            amountText += d.toString()
        }
    }

    fun enterAmount() {
        val normalized = amountText.trim()
        if (normalized.isBlank() || normalized == "-" || normalized == "." || normalized == "-.") {
            amountText = "0" // Reset if invalid input like just "-" or "."
            return
        }
        val number = normalized.toBigDecimalOrNull()
        if (number == null || number.compareTo(BigDecimal.ZERO) == 0) {
            amountText = "0" // Reset if not a valid number or it's zero
            return
        }
        addItem(number, label = null)
        clearAmount()
    }

    var editingItemId by mutableStateOf<String?>(null)
        private set
    var itemNameEditInput by mutableStateOf("")
        private set

    fun startEditItemName(itemId: String) {
        val item = items.find { it.id == itemId }
        if (item != null) {
            editingItemId = itemId
            itemNameEditInput = item.label ?: ""
        }
    }

    fun onItemNameEditInputChange(newName: String) {
        itemNameEditInput = newName
    }

    fun confirmEditItemName() {
        editingItemId?.let { idToEdit ->
            val itemIndex = items.indexOfFirst { it.id == idToEdit }
            if (itemIndex != -1) {
                val oldItem = items[itemIndex]
                val newItem = oldItem.copy(label = itemNameEditInput.trim().takeUnless { it.isBlank() })
                items[itemIndex] = newItem
            }
            viewModelScope.launch { attemptToSaveOrUpdateEvent() }
        }
        editingItemId = null
        itemNameEditInput = ""
    }

    fun cancelEditItemName() {
        editingItemId = null
        itemNameEditInput = ""
    }

    private suspend fun attemptToSaveOrUpdateEvent() {
        val isEventNameExplicitlySet = eventName.isNotBlank() && eventName != generateEventName() // Check if different from default
        val hasItems = items.isNotEmpty()
        val hasParticipants = participants.isNotEmpty()
        val hasBillImages = billImageUris.isNotEmpty() // Check if there are any bill images

        // Save if:
        // 1. There are bill images (event can be saved with just images and a default name).
        // OR
        // 2. Event name is explicitly set AND there are participants OR items.
        val shouldSave = hasBillImages || (isEventNameExplicitlySet && (hasParticipants || hasItems))

        if (shouldSave) {
            // Conditions met, save or update
            val eventToSave = toEvent()
            eventHistoryRepository.saveFullEvent(eventToSave)
            currentEventDatabaseId = eventToSave.id // Ensure we store the ID used (new or existing)
        } else {
            // Conditions not met for proactive saving.
        }
    }

    // Renamed for clarity, or keep as addBillImage if it's the sole public entry point for single images
    fun addSingleBillImageAndSave(uriString: String) { 
        if (uriString.isBlank()) return
        billImageUris.add(uriString)
        viewModelScope.launch { attemptToSaveOrUpdateEvent() }
    }

    // New function for batch processing from image picker
    fun addMultipleBillImagesAndSave(uriStrings: List<String>) {
        val validUris = uriStrings.filter { !it.isBlank() }
        if (validUris.isEmpty()) return

        billImageUris.addAll(validUris)
        // Only trigger a save if we actually added something
        viewModelScope.launch { attemptToSaveOrUpdateEvent() }
    }

    fun resetToNewEvent() {
        currentEventDatabaseId = null // Critical for ensuring a new DB entry
        participants.clear()
        items.clear()
        selectedByItemId.clear()
        billImageUris.clear() // Clear bill images
        eventName = generateEventName() // Reset to a new default name
        amountText = "0" // Reset keypad input

        // Reset any UI states related to editing or adding
        cancelEditItemName()
        cancelAddParticipant()

        // Optionally, you might want to log this or perform other cleanup
        // For instance, if you had loaded an event to edit, this would discard changes
    }

    suspend fun loadEventForEditing(eventId: String) {
        resetToNewEvent() // Clear current state first

        val eventDetails = eventHistoryRepository.getEventDetails(eventId) // Use correct repository method

        if (eventDetails != null) {
            val domainEvent = mapEventWithDetailsToDomainEvent(eventDetails)

            currentEventDatabaseId = domainEvent.id
            eventName = domainEvent.name
            billImageUris.addAll(domainEvent.billImageUris) // Load bill image URIs

            participants.addAll(domainEvent.participants)

            val loadedItemDrafts = domainEvent.items.map { di -> // di for domainItem
                ItemDraft(id = di.id, label = di.label, amount = di.amount)
            }
            items.addAll(loadedItemDrafts)

            // Reconstruct selectedByItemId
            domainEvent.items.forEach { di -> // di for domainItem
                if (di.taggedParticipantIds.isNotEmpty() && !di.taggedParticipantIds.contains("ALL")) {
                    selectedByItemId[di.id] = di.taggedParticipantIds.toMutableSet()
                }
            }
        }
        // Else: event not found, perhaps log an error or handle as needed
    }

    private fun mapEventWithDetailsToDomainEvent(eventDetails: me.seta.vacset.qrwari.data.storage.EventWithDetails): me.seta.vacset.qrwari.data.model.Event {
        val domainParticipants = eventDetails.participants.map { entity ->
            Participant(
                id = entity.id,
                name = entity.name
            )
        }

        val domainItems = eventDetails.items.map { entity ->
            Item(
                id = entity.id,
                label = entity.label,
                amount = entity.amount,
                taggedParticipantIds = entity.taggedParticipantIds // Directly from ItemEntity
            )
        }

        return me.seta.vacset.qrwari.data.model.Event(
            id = eventDetails.event.id,
            name = eventDetails.event.name,
            createdAt = eventDetails.event.createdAt,
            participants = domainParticipants,
            items = domainItems,
            billImageUris = eventDetails.event.billImageUris // Map from EventEntity
        )
    }
}

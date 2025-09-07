package me.seta.vacset.qrwari.presentation.state // Updated package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.seta.vacset.qrwari.data.repository.EventHistoryRepository
import me.seta.vacset.qrwari.data.storage.EventEntity
// EventHistoryItemUiState is in the same package, so no explicit import needed if it's in a separate file
// If they were in the same file, no import is needed. If in separate files in the same package,
// also no import needed.
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class EventHistoryViewModel(
    private val eventHistoryRepository: EventHistoryRepository
) : ViewModel() {

    private val _eventHistory = MutableStateFlow<List<EventHistoryItemUiState>>(emptyList())
    val eventHistory: StateFlow<List<EventHistoryItemUiState>> = _eventHistory.asStateFlow()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    init {
        loadEventHistory()
    }

    fun loadEventHistory() { // Made public for potential pull-to-refresh later
        viewModelScope.launch {
            val eventsFromDb = eventHistoryRepository.getEventHistoryList()
            _eventHistory.update {
                eventsFromDb.map { entity ->
                    mapToUiState(entity)
                }
            }
        }
    }

    private fun mapToUiState(entity: EventEntity): EventHistoryItemUiState {
        return EventHistoryItemUiState(
            id = entity.id,
            name = entity.name,
            creationDateTimeFormatted = formatInstant(entity.createdAt)
        )
    }

    private fun formatInstant(instant: Instant): String {
        return try {
            dateTimeFormatter.format(instant)
        } catch (e: Exception) {
            instant.toString() // Fallback
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            eventHistoryRepository.deleteEventById(eventId)
            loadEventHistory() // Refresh the list
        }
    }

    fun clearAllEvents() {
        viewModelScope.launch {
            eventHistoryRepository.clearAllEvents()
            loadEventHistory() // Refresh the list
        }
    }
}

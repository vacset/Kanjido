package me.seta.vacset.qrwari.presentation.state // Correct package

// Represents a single item in the event history list for UI purposes
data class EventHistoryItemUiState(
    val id: String,
    val name: String,
    val creationDateTimeFormatted: String // e.g., "26 Oct 2023, 10:30 AM"
)

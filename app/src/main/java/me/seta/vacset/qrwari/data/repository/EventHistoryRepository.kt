package me.seta.vacset.qrwari.data.repository

import me.seta.vacset.qrwari.data.storage.EventDao
import me.seta.vacset.qrwari.data.storage.EventEntity
import me.seta.vacset.qrwari.data.storage.EventWithDetails
import me.seta.vacset.qrwari.data.model.Event as DomainEvent // Alias for clarity

/**
 * Repository for handling event history data operations.
 * It abstracts the data source (Room database via EventDao) from the ViewModels.
 */
class EventHistoryRepository(private val eventDao: EventDao) {

    /**
     * Saves a complete event (including its items and participants) to the database.
     *
     * @param domainEvent The event object from the domain layer.
     */
    suspend fun saveFullEvent(domainEvent: DomainEvent) {
        eventDao.insertFullEvent(domainEvent)
    }

    /**
     * Retrieves a list of all events with basic information (no items or participants).
     * Suitable for displaying a history overview.
     *
     * @return A list of [EventEntity] objects.
     */
    suspend fun getEventHistoryList(): List<EventEntity> {
        return eventDao.getAllEventEntities()
    }

    /**
     * Retrieves a specific event along with all its details (items and participants).
     *
     * @param eventId The ID of the event to retrieve.
     * @return An [EventWithDetails] object if found, otherwise null.
     */
    suspend fun getEventDetails(eventId: String): EventWithDetails? {
        return eventDao.getEventWithDetails(eventId)
    }

    /**
     * Deletes an event and its associated data (items, participant links) from the database.
     *
     * @param eventId The ID of the event to delete.
     * @return The number of event rows deleted (typically 1 if successful, 0 if not found).
     */
    suspend fun deleteEventById(eventId: String): Int {
        return eventDao.deleteEventById(eventId)
    }

    // Future considerations:
    // - Expose these as Flows for reactive updates in ViewModels.
    // - Add methods for updating existing events.
}

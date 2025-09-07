package me.seta.vacset.qrwari.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import me.seta.vacset.qrwari.data.model.Event as DomainEvent // Alias for clarity

@Dao
interface EventDao {

    /**
     * Inserts or updates a complete event, including its participants and items.
     * This is a transactional operation to ensure data consistency.
     *
     * If the event (based on ID) already exists, it will be updated.
     * New participants and items will be inserted.
     * Existing participants and items linked to this event (but not in the current
     * domainEvent.participants or domainEvent.items lists) are NOT automatically removed.
     * Consider adding logic to clear old items/participants if replacement is desired.
     */
    @Transaction
    suspend fun insertFullEvent(domainEvent: DomainEvent) {
        // 1. Insert/Update the EventEntity
        val eventEntity = EventEntity(
            id = domainEvent.id,
            name = domainEvent.name,
            createdAt = domainEvent.createdAt,
            billImageUris = domainEvent.billImageUris // Ensure this is mapped
        )
        insertEvent(eventEntity) // Assuming this is an @Insert(onConflict = OnConflictStrategy.REPLACE) or similar

        // 2. Clear existing links and items for this event (optional, for "replace" behavior)
        // For a true "replace", you might want to delete existing items and participant cross-refs
        // deleteItemsForEvent(domainEvent.id)
        // deleteEventParticipantCrossRefsForEvent(domainEvent.id)

        // 3. Insert Participants and their links to the Event
        val participantEntities = domainEvent.participants.map { p ->
            ParticipantEntity(id = p.id, name = p.name)
        }
        insertParticipants(participantEntities) // Should handle conflicts (e.g., ignore or replace)

        val crossRefs = domainEvent.participants.map { p ->
            EventParticipantCrossRef(eventId = domainEvent.id, participantId = p.id)
        }
        insertEventParticipantCrossRefs(crossRefs) // Should handle conflicts

        // 4. Insert Items
        val itemEntities = domainEvent.items.map { i ->
            ItemEntity(
                id = i.id,
                eventId = domainEvent.id,
                label = i.label,
                amount = i.amount,
                taggedParticipantIds = i.taggedParticipantIds
            )
        }
        insertItems(itemEntities) // Should handle conflicts
    }

    // --- Helper Insert/Query/Delete methods for individual entities/relations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Or IGNORE if participants are global and might exist
    suspend fun insertParticipants(participants: List<ParticipantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Or IGNORE
    suspend fun insertItems(items: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventParticipantCrossRefs(crossRefs: List<EventParticipantCrossRef>)

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventWithDetails(eventId: String): EventWithDetails? // This needs to be @Transaction too

    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    suspend fun getAllEventEntities(): List<EventEntity> // For simple history list

    // Methods for deleting event and related data
    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String): Int // Returns number of rows deleted

    @Query("DELETE FROM items WHERE eventId = :eventId")
    suspend fun deleteItemsForEvent(eventId: String)

    @Query("DELETE FROM event_participant_cross_ref WHERE eventId = :eventId")
    suspend fun deleteEventParticipantCrossRefsForEvent(eventId: String)
    
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents(): Int


    // Might need:
    // @Query("DELETE FROM participants WHERE id NOT IN (SELECT DISTINCT participantId FROM event_participant_cross_ref)")
    // suspend fun deleteOrphanedParticipants()

    // Query to get all events with full details (potentially heavy)
    @Transaction
    @Query("SELECT * FROM events")
    suspend fun getAllEventsWithDetails(): List<EventWithDetails>
}

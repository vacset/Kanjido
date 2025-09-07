package me.seta.vacset.qrwari.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
// Using the domain model for the insertFullEvent for convenience, as discussed.
// Ensure your project structure allows this import or adjust as needed.
import me.seta.vacset.qrwari.data.model.Event as DomainEvent

@Dao
interface EventDao {

    @Transaction
    suspend fun insertFullEvent(domainEvent: DomainEvent) {
        val eventEntity = EventEntity(
            id = domainEvent.id,
            name = domainEvent.name,
            createdAt = domainEvent.createdAt
        )
        insertEvent(eventEntity)

        val participantEntities = domainEvent.participants.map {
            ParticipantEntity(id = it.id, name = it.name)
        }
        insertParticipants(participantEntities) // Ignores duplicates

        val itemEntities = domainEvent.items.map {
            ItemEntity(
                id = it.id,
                eventId = eventEntity.id,
                label = it.label,
                amount = it.amount,
                taggedParticipantIds = it.taggedParticipantIds
            )
        }
        insertItems(itemEntities)

        val crossRefs = domainEvent.participants.map {
            EventParticipantCrossRef(eventId = eventEntity.id, participantId = it.id)
        }
        insertEventParticipantCrossRefs(crossRefs)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipants(participants: List<ParticipantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventParticipantCrossRefs(crossRefs: List<EventParticipantCrossRef>)

    @Transaction
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventWithDetails(eventId: String): EventWithDetails?

    @Transaction
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    suspend fun getAllEventsWithDetails(): List<EventWithDetails>

    /**
     * Retrieves all events (basic info only, no items or participants)
     * for display in a history list.
     */
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    suspend fun getAllEventEntities(): List<EventEntity> // New method for history screen

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String): Int
}

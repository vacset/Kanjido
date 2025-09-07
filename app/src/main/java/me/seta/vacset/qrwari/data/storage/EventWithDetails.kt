package me.seta.vacset.qrwari.data.storage

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Data class to hold an EventEntity and its related ItemEntities and ParticipantEntities.
 * This is used for querying comprehensive event details.
 */
data class EventWithDetails(
    @Embedded
    val event: EventEntity,

    @Relation(
        parentColumn = "id", // EventEntity.id
        entityColumn = "eventId" // ItemEntity.eventId
    )
    val items: List<ItemEntity>,

    @Relation(
        parentColumn = "id", // EventEntity.id
        entityColumn = "id", // ParticipantEntity.id
        associateBy = Junction(
            value = EventParticipantCrossRef::class,
            parentColumn = "eventId", // Column in EventParticipantCrossRef linking to EventEntity
            entityColumn = "participantId" // Column in EventParticipantCrossRef linking to ParticipantEntity
        )
    )
    val participants: List<ParticipantEntity>
)

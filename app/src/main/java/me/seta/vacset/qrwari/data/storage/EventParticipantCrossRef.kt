package me.seta.vacset.qrwari.data.storage

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "event_participant_cross_ref",
    primaryKeys = ["eventId", "participantId"], // Composite primary key
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE // If an event is deleted, these refs are deleted
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.CASCADE // If a participant is deleted, these refs are deleted
                                         // Consider RESTRICT if a participant cannot be deleted if in an event.
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["participantId"])
    ]
)
data class EventParticipantCrossRef(
    val eventId: String,
    val participantId: String
)

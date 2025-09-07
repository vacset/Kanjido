package me.seta.vacset.qrwari.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participants")
data class ParticipantEntity(
    @PrimaryKey
    val id: String, // Matches Participant.id (UUID string)
    val name: String
)

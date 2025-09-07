package me.seta.vacset.qrwari.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant

@Entity(tableName = "events")
@TypeConverters(StorageTypeConverters::class) // To handle Instant
data class EventEntity(
    @PrimaryKey
    val id: String, // Matches Event.id (UUID string)
    val name: String,
    val createdAt: Instant // Was Instant in Model.kt
)

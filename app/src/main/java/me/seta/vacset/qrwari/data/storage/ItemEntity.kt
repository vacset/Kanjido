package me.seta.vacset.qrwari.data.storage

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.math.BigDecimal

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE // If an event is deleted, its items are also deleted
        )
    ],
    indices = [Index(value = ["eventId"]) // Index on eventId for faster queries
    ]
)
@TypeConverters(StorageTypeConverters::class) // For BigDecimal and Set<String>
data class ItemEntity(
    @PrimaryKey
    val id: String, // Matches Item.id (UUID string)
    val eventId: String, // Foreign key to EventEntity
    val label: String?,
    val amount: BigDecimal,
    val taggedParticipantIds: Set<String> // Was Set<String> in Model.kt
)

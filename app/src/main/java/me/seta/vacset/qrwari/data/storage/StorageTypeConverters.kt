package me.seta.vacset.qrwari.data.storage

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.Instant

class StorageTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun fromBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun bigDecimalToString(bigDecimal: BigDecimal?): String? {
        return bigDecimal?.toPlainString()
    }

    // Converter for Set<String> (e.g., for taggedParticipantIds in ItemEntity)
    @TypeConverter
    fun fromStringSet(value: String?): Set<String>? {
        // Splits by comma, trims whitespace, filters out empty strings that might result from trailing commas or multiple commas
        return value?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
    }

    @TypeConverter
    fun stringSetToString(set: Set<String>?): String? {
        return set?.joinToString(",")
    }

    // --- Converters for List<String> for billImageUris ---
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        // Splits by comma, trims whitespace, filters out empty strings
        return value?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun stringListToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}

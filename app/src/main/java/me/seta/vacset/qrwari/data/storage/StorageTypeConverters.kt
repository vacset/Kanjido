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
    fun fromBigDecimalString(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    // Using a simple comma-separated string for Set<String>.
    // Ensure your participant IDs don't contain commas.
    // If they might, or for more robustness, storing as a JSON string is better.
    @TypeConverter
    fun fromStringSetToString(value: Set<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun fromStringToSetString(value: String?): Set<String>? {
        return value?.split(',')?.filter { it.isNotBlank() }?.toSet()
    }

    // For List<String> used in billImageUris
    // Using a simple comma-separated string.
    // Ensure your URIs don't contain commas or handle encoding/decoding if they might.
    // Storing as a JSON string array is a more robust alternative for complex strings.
    @TypeConverter
    fun fromStringToListString(value: String?): List<String>? {
        return value?.split(',')?.filter { it.isNotBlank() }?.toList()
    }

    @TypeConverter
    fun fromListStringToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}

package me.seta.vacset.qrwari.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun generateEventName(now: ZonedDateTime = ZonedDateTime.now()): String {
    val bucket = when (now.hour) {
        in 5..11 -> "morning"
        in 12..13 -> "noon"
        in 14..17 -> "afternoon"
        in 18..21 -> "evening"
        else -> "night"
    }
    val day = now.format(DateTimeFormatter.ofPattern("d MMM"))
    return "$day $bucket"
}
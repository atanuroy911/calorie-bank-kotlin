package com.roy.caloriebank.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

private fun Instant.toLocalDate(): LocalDate = this.atZone(ZoneId.systemDefault()).toLocalDate()

fun Instant.isSameDay(other: Instant): Boolean = this.toLocalDate() == other.toLocalDate()

fun Instant.startOfDay(): Instant =
    this.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()

fun Instant.endOfDay(): Instant =
    this.toLocalDate().atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant()

fun Instant.isToday(): Boolean = this.toLocalDate() == LocalDate.now(ZoneId.systemDefault())

fun Instant.isYesterday(): Boolean =
    this.toLocalDate() == LocalDate.now(ZoneId.systemDefault()).minusDays(1)

fun Instant.relativeLabel(): String {
    return when {
        this.isToday() -> "Today"
        this.isYesterday() -> "Yesterday"
        else -> {
            val date = this.toLocalDate()
            val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            "$month ${date.dayOfMonth}"
        }
    }
}

fun Instant.timeLabel(): String {
    val zoned = this.atZone(ZoneId.systemDefault())
    val hour24 = zoned.hour
    val minute = zoned.minute
    val amPm = if (hour24 < 12) "AM" else "PM"
    var hour12 = hour24 % 12
    if (hour12 == 0) hour12 = 12
    val minuteStr = minute.toString().padStart(2, '0')
    return "$hour12:$minuteStr $amPm"
}

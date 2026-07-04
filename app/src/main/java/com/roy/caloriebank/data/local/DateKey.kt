package com.roy.caloriebank.data.local

import java.time.Instant
import java.time.ZoneId

/** Formats an [Instant] as a local 'YYYY-MM-DD' key used for daily grouping. */
fun dateKeyOf(instant: Instant): String {
    val local = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return "%04d-%02d-%02d".format(local.year, local.monthValue, local.dayOfMonth)
}

package com.roy.caloriebank.domain.util

import kotlin.math.abs
import kotlin.math.roundToInt

fun Number.kcalFormatted(): String {
    val value = this.toDouble()
    val rounded = value.roundToInt()
    return if (rounded >= 1000) {
        val thousands = rounded / 1000
        val remainder = rounded % 1000
        "$thousands,${remainder.toString().padStart(3, '0')}"
    } else {
        rounded.toString()
    }
}

fun Number.kcalShort(): String {
    val value = this.toDouble()
    val rounded = value.roundToInt()
    return if (rounded >= 1000) {
        val k = rounded / 1000.0
        "${"%.1f".format(k)}k"
    } else {
        rounded.toString()
    }
}

fun Number.gramFormatted(): String = "%.1fg".format(this.toDouble())

fun Number.clampedProgress(): Double = this.toDouble().coerceIn(0.0, 1.0)

fun Number.percentOf(total: Number): Double {
    val t = total.toDouble()
    if (t == 0.0) return 0.0
    return (this.toDouble() / t * 100).coerceIn(0.0, 999.0)
}

fun Int.signedKcal(): String =
    if (this >= 0) "+${this.kcalFormatted()} kcal" else "-${abs(this).kcalFormatted()} kcal"

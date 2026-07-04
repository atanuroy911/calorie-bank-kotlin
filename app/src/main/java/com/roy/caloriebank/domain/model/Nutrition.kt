package com.roy.caloriebank.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Macros(
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val fiberG: Double = 0.0,
    val sugarG: Double = 0.0,
    val saturatedFatG: Double = 0.0,
    val transFatG: Double = 0.0,
    val cholesterolMg: Double = 0.0,
) {
    operator fun plus(other: Macros): Macros = Macros(
        proteinG = proteinG + other.proteinG,
        carbsG = carbsG + other.carbsG,
        fatG = fatG + other.fatG,
        fiberG = fiberG + other.fiberG,
        sugarG = sugarG + other.sugarG,
        saturatedFatG = saturatedFatG + other.saturatedFatG,
        transFatG = transFatG + other.transFatG,
        cholesterolMg = cholesterolMg + other.cholesterolMg,
    )
}

@Serializable
data class Micros(
    val sodiumMg: Double = 0.0,
    val potassiumMg: Double = 0.0,
    val calciumMg: Double = 0.0,
    val ironMg: Double = 0.0,
    val magnesiumMg: Double = 0.0,
    val zincMg: Double = 0.0,
    val phosphorusMg: Double = 0.0,
    val vitaminCMg: Double = 0.0,
    val vitaminDUg: Double = 0.0,
    val vitaminB12Ug: Double = 0.0,
    val folateMcg: Double = 0.0,
    val vitaminAUg: Double = 0.0,
    val vitaminEMg: Double = 0.0,
    val vitaminKUg: Double = 0.0,
) {
    operator fun plus(other: Micros): Micros = Micros(
        sodiumMg = sodiumMg + other.sodiumMg,
        potassiumMg = potassiumMg + other.potassiumMg,
        calciumMg = calciumMg + other.calciumMg,
        ironMg = ironMg + other.ironMg,
        magnesiumMg = magnesiumMg + other.magnesiumMg,
        zincMg = zincMg + other.zincMg,
        phosphorusMg = phosphorusMg + other.phosphorusMg,
        vitaminCMg = vitaminCMg + other.vitaminCMg,
        vitaminDUg = vitaminDUg + other.vitaminDUg,
        vitaminB12Ug = vitaminB12Ug + other.vitaminB12Ug,
        folateMcg = folateMcg + other.folateMcg,
        vitaminAUg = vitaminAUg + other.vitaminAUg,
        vitaminEMg = vitaminEMg + other.vitaminEMg,
        vitaminKUg = vitaminKUg + other.vitaminKUg,
    )
}

object DailyReferenceValues {
    const val fiberG = 28.0
    const val sugarG = 50.0
    const val saturatedFatG = 20.0
    const val cholesterolMg = 300.0
    const val sodiumMg = 2300.0
    const val potassiumMg = 4700.0
    const val calciumMg = 1300.0
    const val ironMg = 18.0
    const val magnesiumMg = 420.0
    const val zincMg = 11.0
    const val phosphorusMg = 1250.0
    const val vitaminCMg = 90.0
    const val vitaminDUg = 20.0
    const val vitaminB12Ug = 2.4
    const val folateMcg = 400.0
    const val vitaminAUg = 900.0
    const val vitaminEMg = 15.0
    const val vitaminKUg = 120.0
}

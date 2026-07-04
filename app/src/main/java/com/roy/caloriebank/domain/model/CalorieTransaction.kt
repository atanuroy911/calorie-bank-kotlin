package com.roy.caloriebank.domain.model

import java.time.Instant

enum class TransactionType {
    FoodWithdrawal,
    ExerciseDeposit,
    BankWithdrawal,
    BankDeposit,
    DailySavingsDeposit,
    ManualAdjustment,
}

val TransactionType.isPositive: Boolean
    get() = when (this) {
        TransactionType.ExerciseDeposit,
        TransactionType.BankWithdrawal,
        TransactionType.DailySavingsDeposit -> true
        TransactionType.FoodWithdrawal,
        TransactionType.BankDeposit,
        TransactionType.ManualAdjustment -> false
    }

val TransactionType.label: String
    get() = when (this) {
        TransactionType.FoodWithdrawal -> "Food"
        TransactionType.ExerciseDeposit -> "Exercise"
        TransactionType.BankWithdrawal -> "Bank Withdrawal"
        TransactionType.BankDeposit -> "Savings Deposit"
        TransactionType.DailySavingsDeposit -> "Daily Savings"
        TransactionType.ManualAdjustment -> "Manual Adjustment"
    }

val TransactionType.icon: String
    get() = when (this) {
        TransactionType.FoodWithdrawal -> "🍽️"
        TransactionType.ExerciseDeposit -> "🏃"
        TransactionType.BankWithdrawal -> "🏦"
        TransactionType.BankDeposit -> "💰"
        TransactionType.DailySavingsDeposit -> "📈"
        TransactionType.ManualAdjustment -> "✏️"
    }

data class CalorieTransaction(
    val id: String,
    val timestamp: Instant,
    val type: TransactionType,
    val calories: Int,
    val label: String,
    val foodEntryId: String? = null,
    val exerciseEntryId: String? = null,
    val userId: String,
    val date: String,
) {
    val signedCalories: Int
        get() = when (type) {
            TransactionType.FoodWithdrawal -> -calories
            TransactionType.ExerciseDeposit -> calories
            TransactionType.BankWithdrawal -> calories
            TransactionType.BankDeposit -> -calories
            TransactionType.DailySavingsDeposit -> 0
            TransactionType.ManualAdjustment -> calories
        }

    val bankSignedCalories: Int
        get() = when (type) {
            TransactionType.BankWithdrawal -> -calories
            TransactionType.BankDeposit -> calories
            TransactionType.DailySavingsDeposit -> calories
            else -> 0
        }
}

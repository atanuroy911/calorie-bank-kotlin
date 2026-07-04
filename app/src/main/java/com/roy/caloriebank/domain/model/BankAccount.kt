package com.roy.caloriebank.domain.model

import java.time.Instant

data class BankAccount(
    val userId: String,
    val balance: Int,
    val lastUpdated: Instant,
)

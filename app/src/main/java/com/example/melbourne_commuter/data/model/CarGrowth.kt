package com.example.melbourne_commuter.data.model


data class CarGrowth(
    val growth_percentage: Double,
    val id: Int,
    val increased_cars_amount: Int,
    val state: String,
    val year_from: Int,
    val year_to: Int
)

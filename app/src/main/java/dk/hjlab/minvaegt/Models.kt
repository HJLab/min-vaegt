package dk.hjlab.minvaegt

import java.time.LocalDate

data class Profile(
    val name: String = "",
    val heightCm: Double = 170.0,
    val startWeight: Double = 80.0,
    val goalWeight: Double = 75.0,
    val startDate: String = LocalDate.now().toString()
)

data class WeightEntry(
    val weight: Double,
    val timestamp: Long
)

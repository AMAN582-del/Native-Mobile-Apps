package com.aman.shiftmate.model

enum class TaxRegion(val label: String) {
    ENGLAND_WALES_NI("England, Wales & Northern Ireland"),
    SCOTLAND("Scotland")
}

// The user's profile. The hourly rate here is only a starting default.
data class UserSettings(
    val name: String = "",
    val hourlyRate: Double = 12.0,
    val taxRegion: TaxRegion = TaxRegion.ENGLAND_WALES_NI
)
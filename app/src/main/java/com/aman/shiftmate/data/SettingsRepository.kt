package com.aman.shiftmate.data

import android.content.Context
import com.aman.shiftmate.model.TaxRegion
import com.aman.shiftmate.model.UserSettings

// Stores the profile in app-private SharedPreferences (other apps cannot read it)
class SettingsRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("shiftmate_settings", Context.MODE_PRIVATE)

    fun get(): UserSettings {
        val defaults = UserSettings()
        return UserSettings(
            name = prefs.getString(KEY_NAME, null) ?: defaults.name,
            hourlyRate = prefs.getString(KEY_RATE, null)?.toDoubleOrNull() ?: defaults.hourlyRate,
            taxRegion = runCatching {
                TaxRegion.valueOf(prefs.getString(KEY_REGION, null) ?: "")
            }.getOrDefault(defaults.taxRegion)
        )
    }

    fun save(settings: UserSettings) {
        prefs.edit()
            .putString(KEY_NAME, settings.name)
            .putString(KEY_RATE, settings.hourlyRate.toString())
            .putString(KEY_REGION, settings.taxRegion.name)
            .apply()
    }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_RATE = "hourly_rate"
        const val KEY_REGION = "tax_region"
    }
}
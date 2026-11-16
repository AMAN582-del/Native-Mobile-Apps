package com.aman.shiftmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.aman.shiftmate.data.SettingsRepository
import com.aman.shiftmate.model.TaxRegion
import com.aman.shiftmate.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

/** Everything the Profile screen shows. */
data class ProfileUiState(
    val name: String = "",
    val rateText: String = "",
    val taxRegion: TaxRegion = TaxRegion.ENGLAND_WALES_NI,
    val errorMessage: String? = null,
    val saved: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val s = settingsRepository.get()
        _uiState.value = ProfileUiState(
            name = s.name,
            rateText = String.format(Locale.US, "%.2f", s.hourlyRate),
            taxRegion = s.taxRegion
        )
    }

    // Length limits stop absurdly long input
    fun onNameChange(value: String) {
        if (value.length <= 40) {
            _uiState.update { it.copy(name = value, saved = false, errorMessage = null) }
        }
    }

    fun onRateChange(value: String) {
        if (value.length <= 8) {
            _uiState.update { it.copy(rateText = value, saved = false, errorMessage = null) }
        }
    }

    fun onRegionChange(region: TaxRegion) {
        _uiState.update { it.copy(taxRegion = region, saved = false, errorMessage = null) }
    }

    fun onSave() {
        val s = _uiState.value
        val rate = s.rateText.trim().toDoubleOrNull()
        if (rate == null || !rate.isFinite() || rate <= 0.0 || rate > 500.0) {
            _uiState.update { it.copy(errorMessage = "Enter an hourly rate between 0.01 and 500.", saved = false) }
            return
        }
        settingsRepository.save(UserSettings(s.name.trim(), rate, s.taxRegion))
        _uiState.update { it.copy(errorMessage = null, saved = true) }
    }
}
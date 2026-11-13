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

// Everything the Profile screen needs to display
data class ProfileUiState(
    val name: String = "",
    val rateText: String = "",
    val taxRegion: TaxRegion = TaxRegion.ENGLAND_WALES_NI,
    val errorMessage: String? = null,
    val saved: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val settings = repository.get()
        _uiState.value = ProfileUiState(
            name = settings.name,
            rateText = "%.2f".format(Locale.US, settings.hourlyRate),
            taxRegion = settings.taxRegion
        )
    }

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value.take(40), saved = false) }

    fun onRateChange(value: String) =
        _uiState.update { it.copy(rateText = value.take(8), saved = false, errorMessage = null) }

    fun onRegionChange(region: TaxRegion) =
        _uiState.update { it.copy(taxRegion = region, saved = false) }

    fun onSave() {
        val state = _uiState.value
        val rate = state.rateText.toDoubleOrNull()
        // Input validation: reject empty, non-numeric or unrealistic rates
        if (rate == null || rate <= 0.0 || rate > 500.0) {
            _uiState.update { it.copy(errorMessage = "Enter a valid hourly rate, for example 12.50") }
            return
        }
        repository.save(UserSettings(name = state.name.trim(), hourlyRate = rate, taxRegion = state.taxRegion))
        _uiState.update { it.copy(errorMessage = null, saved = true) }
    }
}
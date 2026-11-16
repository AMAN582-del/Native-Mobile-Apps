package com.aman.shiftmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiftmate.data.LoadedRates
import com.aman.shiftmate.data.RatesSource
import com.aman.shiftmate.data.SettingsRepository
import com.aman.shiftmate.data.ShiftDatabase
import com.aman.shiftmate.data.ShiftRepository
import com.aman.shiftmate.data.TaxRatesRepository
import com.aman.shiftmate.domain.PayCalculator
import com.aman.shiftmate.domain.TaxRates
import com.aman.shiftmate.model.Shift
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShiftRepository(ShiftDatabase.getInstance(application).shiftDao())
    private val settingsRepository = SettingsRepository(application)
    private val ratesRepository = TaxRatesRepository(application)

    private val period = MutableStateFlow(SummaryPeriod.WEEK)
    private val refreshTick = MutableStateFlow(0)
    private val loadedRates = MutableStateFlow(LoadedRates(TaxRates.DEFAULT, RatesSource.BUILT_IN))

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        // Download (or fall back to) the tax rates once when the screen is first created
        viewModelScope.launch { loadedRates.value = ratesRepository.load() }

        // Recalculate whenever shifts, period, profile refresh or tax rates change
        viewModelScope.launch {
            combine(repository.shifts, period, refreshTick, loadedRates) { shifts, p, _, lr ->
                build(shifts, p, lr)
            }
                .catch {
                    _uiState.update { s ->
                        s.copy(isLoading = false, errorMessage = "Could not calculate your earnings.")
                    }
                }
                .collect { fresh ->
                    _uiState.update { fresh.copy(isRefreshingRates = it.isRefreshingRates) }
                }
        }
    }

    fun selectPeriod(p: SummaryPeriod) { period.value = p }

    /** Called when the tab is shown, so a changed tax region in Profile is picked up. */
    fun refresh() { refreshTick.update { it + 1 } }

    /** Called by the "Refresh tax rates" button. */
    fun refreshRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingRates = true) }
            loadedRates.value = ratesRepository.load()
            _uiState.update { it.copy(isRefreshingRates = false) }
        }
    }

    private fun build(shifts: List<Shift>, p: SummaryPeriod, lr: LoadedRates): SummaryUiState {
        val start = periodStart(p)
        val inPeriod = shifts.filter { it.endTime != null && it.startTime >= start }
        val settings = settingsRepository.get()
        val result = PayCalculator.summarise(inPeriod, p.periodsPerYear, settings.taxRegion, lr.rates)
        return SummaryUiState(
            period = p,
            shiftCount = inPeriod.size,
            result = result,
            taxRegion = settings.taxRegion,
            ratesTaxYear = lr.rates.taxYear,
            ratesSource = lr.source,
            isLoading = false,
            errorMessage = null
        )
    }

    /** Start of this week (Monday 00:00) or this month (1st, 00:00), in local time. */
    private fun periodStart(p: SummaryPeriod): Long {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = Calendar.MONDAY
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        if (p == SummaryPeriod.WEEK) c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        else c.set(Calendar.DAY_OF_MONTH, 1)
        return c.timeInMillis
    }
}
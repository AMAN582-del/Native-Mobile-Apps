package com.aman.shiftmate

import com.aman.shiftmate.data.RatesSource
import com.aman.shiftmate.domain.PayResult
import com.aman.shiftmate.domain.TaxRates
import com.aman.shiftmate.model.TaxRegion

/** Which period the Summary tab shows. */
enum class SummaryPeriod(val label: String, val periodsPerYear: Int) {
    WEEK("This week", 52),
    MONTH("This month", 12)
}

/** Everything the Summary screen needs to draw itself. */
data class SummaryUiState(
    val period: SummaryPeriod = SummaryPeriod.WEEK,
    val shiftCount: Int = 0,
    val result: PayResult = PayResult(0.0, 0.0, 0.0, 0.0),
    val taxRegion: TaxRegion = TaxRegion.ENGLAND_WALES_NI,
    val ratesTaxYear: String = TaxRates.DEFAULT.taxYear,
    val ratesSource: RatesSource = RatesSource.BUILT_IN,
    val isRefreshingRates: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
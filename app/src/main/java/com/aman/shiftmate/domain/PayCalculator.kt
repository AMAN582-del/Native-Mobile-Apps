

package com.aman.shiftmate.domain

import com.aman.shiftmate.model.Shift
import com.aman.shiftmate.model.TaxRegion

/** Tax and NI for one period. */
data class Deductions(val incomeTax: Double, val nationalInsurance: Double)

/** Result for a set of shifts. All money values are estimates. */
data class PayResult(
    val hours: Double,
    val gross: Double,
    val incomeTax: Double,
    val nationalInsurance: Double
) {
    val net: Double get() = gross - incomeTax - nationalInsurance
}

/** Pure Kotlin calculator: no Android code, so it is easy to unit test. */
object PayCalculator {
    private const val MS_PER_HOUR = 3_600_000.0

    /** Paid hours of one shift (end minus start minus unpaid break). Open shifts count as 0. */
    fun hoursWorked(shift: Shift): Double {
        val end = shift.endTime ?: return 0.0
        val hours = (end - shift.startTime) / MS_PER_HOUR - shift.breakMinutes / 60.0
        return if (hours > 0) hours else 0.0
    }

    fun grossPay(shift: Shift): Double = hoursWorked(shift) * shift.hourlyRate

    /** Tax on a yearly income, adding up each band the income reaches into. */
    fun taxOnBands(annualIncome: Double, bands: List<TaxBand>): Double =
        bands.sumOf { band ->
            val upper = band.to ?: Double.MAX_VALUE
            val taxable = minOf(annualIncome, upper) - band.from
            if (taxable > 0) taxable * band.rate else 0.0
        }

    fun niOnIncome(annualIncome: Double, rates: TaxRates): Double {
        val main = (minOf(annualIncome, rates.niUpperLimit) - rates.niThreshold).coerceAtLeast(0.0)
        val upper = (annualIncome - rates.niUpperLimit).coerceAtLeast(0.0)
        return main * rates.niMainRate + upper * rates.niUpperRate
    }

    /**
     * Estimates deductions by annualising: gross x periodsPerYear (52 weekly, 12 monthly),
     * apply the bands, then divide back to the period.
     */
    fun deductions(
        grossForPeriod: Double,
        periodsPerYear: Int,
        region: TaxRegion,
        rates: TaxRates = TaxRates.DEFAULT
    ): Deductions {
        if (grossForPeriod <= 0.0) return Deductions(0.0, 0.0)
        val annual = grossForPeriod * periodsPerYear
        val bands = if (region == TaxRegion.SCOTLAND) rates.scotland else rates.restOfUk
        return Deductions(
            incomeTax = taxOnBands(annual, bands) / periodsPerYear,
            nationalInsurance = niOnIncome(annual, rates) / periodsPerYear
        )
    }

    fun summarise(
        shifts: List<Shift>,
        periodsPerYear: Int,
        region: TaxRegion,
        rates: TaxRates = TaxRates.DEFAULT
    ): PayResult {
        val hours = shifts.sumOf { hoursWorked(it) }
        val gross = shifts.sumOf { grossPay(it) }
        val d = deductions(gross, periodsPerYear, region, rates)
        return PayResult(hours, gross, d.incomeTax, d.nationalInsurance)
    }
}
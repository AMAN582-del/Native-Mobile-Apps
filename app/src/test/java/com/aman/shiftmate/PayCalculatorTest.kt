package com.aman.shiftmate

import com.aman.shiftmate.domain.PayCalculator
import com.aman.shiftmate.model.Shift
import com.aman.shiftmate.model.TaxRegion
import org.junit.Assert.assertEquals
import org.junit.Test

class PayCalculatorTest {
    private val hour = 3_600_000L

    @Test fun zeroHours_givesZeroEverything() {
        val r = PayCalculator.summarise(emptyList(), 52, TaxRegion.ENGLAND_WALES_NI)
        assertEquals(0.0, r.gross, 0.001)
        assertEquals(0.0, r.net, 0.001)
    }

    @Test fun openShift_countsAsZeroHours() {
        val s = Shift(startTime = 0, endTime = null, hourlyRate = 12.0)
        assertEquals(0.0, PayCalculator.hoursWorked(s), 0.001)
    }

    @Test fun shiftAcrossMidnight_minusBreak() {
        // 22:00 to 06:00 = 8h, minus 30 minute break = 7.5h
        val s = Shift(startTime = 0, endTime = 8 * hour, breakMinutes = 30, hourlyRate = 10.0)
        assertEquals(7.5, PayCalculator.hoursWorked(s), 0.001)
        assertEquals(75.0, PayCalculator.grossPay(s), 0.001)
    }

    @Test fun belowPersonalAllowance_noTaxNoNi() {
        val s = Shift(startTime = 0, endTime = 12 * hour, hourlyRate = 12.85) // 154.20
        val r = PayCalculator.summarise(listOf(s), 52, TaxRegion.ENGLAND_WALES_NI)
        assertEquals(154.2, r.gross, 0.01)
        assertEquals(0.0, r.incomeTax, 0.001)
        assertEquals(0.0, r.nationalInsurance, 0.001)
        assertEquals(154.2, r.net, 0.01)
    }

    @Test fun basicRate_30000() {
        val d = PayCalculator.deductions(30000.0, 1, TaxRegion.ENGLAND_WALES_NI)
        assertEquals(3486.0, d.incomeTax, 0.01)          // (30000-12570) x 20%
        assertEquals(1394.4, d.nationalInsurance, 0.01)  // (30000-12570) x 8%
    }

    @Test fun crossingIntoHigherRate_60000() {
        val d = PayCalculator.deductions(60000.0, 1, TaxRegion.ENGLAND_WALES_NI)
        assertEquals(11432.0, d.incomeTax, 0.01)         // 7540 + 3892
        assertEquals(3210.6, d.nationalInsurance, 0.01)  // 3016 + 194.6
    }

    @Test fun scottishBands_30000() {
        val d = PayCalculator.deductions(30000.0, 1, TaxRegion.SCOTLAND)
        assertEquals(3451.07, d.incomeTax, 0.01)         // 753.73 + 2597.80 + 99.54
    }
}
package com.aman.shiftmate.domain

/** One income band. [to] = null means "no upper limit". */
data class TaxBand(val from: Double, val to: Double?, val rate: Double)

/** All rates in one place so they can later be loaded from the internet. */
data class TaxRates(
    val taxYear: String,
    val restOfUk: List<TaxBand>,
    val scotland: List<TaxBand>,
    val niThreshold: Double,
    val niUpperLimit: Double,
    val niMainRate: Double,
    val niUpperRate: Double
) {
    companion object {
        /** Bundled defaults, used offline. Sources: gov.scot, gov.uk (2026-27). */
        val DEFAULT = TaxRates(
            taxYear = "2026-27",
            restOfUk = listOf(
                TaxBand(12570.0, 50270.0, 0.20),
                TaxBand(50270.0, 125140.0, 0.40),
                TaxBand(125140.0, null, 0.45)
            ),
            scotland = listOf(
                TaxBand(12570.0, 16537.0, 0.19),
                TaxBand(16537.0, 29526.0, 0.20),
                TaxBand(29526.0, 43662.0, 0.21),
                TaxBand(43662.0, 75000.0, 0.42),
                TaxBand(75000.0, 125140.0, 0.45),
                TaxBand(125140.0, null, 0.48)
            ),
            niThreshold = 12570.0,
            niUpperLimit = 50270.0,
            niMainRate = 0.08,
            niUpperRate = 0.02
        )
    }
}
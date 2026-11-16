package com.aman.shiftmate.data

import android.content.Context
import android.util.Log
import com.aman.shiftmate.domain.TaxBand
import com.aman.shiftmate.domain.TaxRates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Where the rates came from, shown to the user. */
enum class RatesSource(val label: String) {
    ONLINE("online"),
    CACHED("saved copy"),
    BUILT_IN("built-in")
}

data class LoadedRates(val rates: TaxRates, val source: RatesSource)

/**
 * Loads tax rates: download -> saved copy -> built-in defaults.
 * The downloaded JSON is validated before use, because remote data is not trusted.
 */
class TaxRatesRepository(context: Context) {

    private val prefs = context.getSharedPreferences("shiftmate_rates", Context.MODE_PRIVATE)

    suspend fun load(): LoadedRates = withContext(Dispatchers.IO) {
        try {
            val text = download()
            val rates = parse(text)          // throws if the data is invalid
            prefs.edit().putString(KEY_JSON, text).apply()
            Log.i(TAG, "Loaded rates online")
            LoadedRates(rates, RatesSource.ONLINE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Only the error type and message are logged; no user data is involved
            Log.w(TAG, "Download failed: ${e.javaClass.simpleName} ${e.message}")
            fallback()                       // offline, timeout, bad JSON, bad values
        }
    }

    private fun fallback(): LoadedRates {
        val saved = prefs.getString(KEY_JSON, null)
        if (saved != null) {
            try {
                return LoadedRates(parse(saved), RatesSource.CACHED)
            } catch (e: Exception) {
                // saved copy is damaged: fall through to the built-in rates
                Log.w(TAG, "Saved copy unusable: ${e.javaClass.simpleName}")
            }
        }
        return LoadedRates(TaxRates.DEFAULT, RatesSource.BUILT_IN)
    }

    private fun download(): String {
        val conn = URL(RATES_URL).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${conn.responseCode}")
            }
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            if (text.length > 20_000) throw IOException("Response too large")
            return text
        } finally {
            conn.disconnect()
        }
    }

    private fun parse(text: String): TaxRates {
        val o = JSONObject(text)
        val ni = o.getJSONObject("nationalInsurance")
        val rates = TaxRates(
            taxYear = o.getString("taxYear"),
            restOfUk = bands(o.getJSONArray("restOfUk")),
            scotland = bands(o.getJSONArray("scotland")),
            niThreshold = ni.getDouble("threshold"),
            niUpperLimit = ni.getDouble("upperLimit"),
            niMainRate = ni.getDouble("mainRate"),
            niUpperRate = ni.getDouble("upperRate")
        )
        validate(rates)
        return rates
    }

    private fun bands(arr: JSONArray): List<TaxBand> =
        (0 until arr.length()).map { i ->
            val b = arr.getJSONObject(i)
            TaxBand(
                from = b.getDouble("from"),
                to = if (b.isNull("to")) null else b.getDouble("to"),
                rate = b.getDouble("rate")
            )
        }

    /** Reject nonsense values so a tampered or broken file cannot produce silly results. */
    private fun validate(r: TaxRates) {
        fun bandsOk(list: List<TaxBand>) = list.isNotEmpty() && list.all {
            it.rate in 0.0..1.0 && it.from >= 0.0 && (it.to == null || it.to > it.from)
        }
        require(bandsOk(r.restOfUk) && bandsOk(r.scotland)) { "Invalid tax bands" }
        require(r.niMainRate in 0.0..1.0 && r.niUpperRate in 0.0..1.0) { "Invalid NI rates" }
        require(r.niThreshold >= 0.0 && r.niUpperLimit > r.niThreshold) { "Invalid NI limits" }
    }

    companion object {
        private const val TAG = "TaxRates"
        private const val KEY_JSON = "rates_json"
        // Public JSON hosted in the project's GitHub repo. HTTPS only.
        const val RATES_URL =
            "https://raw.githubusercontent.com/AMAN582-del/Native-Mobile-Apps/main/tax-rates.json"
    }
}
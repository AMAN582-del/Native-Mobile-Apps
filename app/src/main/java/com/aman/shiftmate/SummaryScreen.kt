package com.aman.shiftmate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

/** Stateful wrapper: connects the ViewModel to the stateless content (state hoisting). */
@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: SummaryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    SummaryContent(
        state = state,
        onPeriodSelected = viewModel::selectPeriod,
        onRefreshRates = viewModel::refreshRates,
        modifier = modifier
    )
}

@Composable
fun SummaryContent(
    state: SummaryUiState,
    onPeriodSelected: (SummaryPeriod) -> Unit,
    onRefreshRates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val r = state.result
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Earnings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryPeriod.values().forEach { p ->
                FilterChip(
                    selected = state.period == p,
                    onClick = { onPeriodSelected(p) },
                    label = { Text(p.label) }
                )
            }
        }

        // Big take-home card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("EST. TAKE-HOME", style = MaterialTheme.typography.labelMedium)
                Text(
                    money(r.net),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${state.shiftCount} shift(s) · ${String.format(Locale.UK, "%.1f", r.hours)} h",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Breakdown card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryRow("Gross pay", money(r.gross))
                SummaryRow("Income tax", "-" + money(r.incomeTax))
                SummaryRow("National Insurance", "-" + money(r.nationalInsurance))
                SummaryRow("Take-home (estimate)", money(r.net), bold = true)
            }
        }

        Text(
            "Estimate only. Uses ${state.ratesTaxYear} rates for ${state.taxRegion.label}. " +
                    "Student loan and other deductions are not included.",
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            "Tax rates: ${state.ratesTaxYear} (${state.ratesSource.label})",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedButton(onClick = onRefreshRates, enabled = !state.isRefreshingRates) {
            Text(if (state.isRefreshingRates) "Refreshing…" else "Refresh tax rates")
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable

private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

private fun money(value: Double): String = String.format(Locale.UK, "£%,.2f", value)
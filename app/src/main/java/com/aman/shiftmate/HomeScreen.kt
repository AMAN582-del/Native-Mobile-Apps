package com.aman.shiftmate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Stateful wrapper: owns the ViewModel and collects its state
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refreshWeek() }
    HomeContent(
        state = state,
        onToggle = viewModel::onClockToggle,
        modifier = modifier
    )
}

// Stateless composable: receives state and event callbacks (state hoisting)
@Composable
fun HomeContent(
    state: HomeUiState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { SimpleDateFormat("EEEE d MMMM", Locale.UK).format(Date()) }
    val weekRange = remember { weekRangeLabel() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(today, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            Text("ShiftMate", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }

        // Clock card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.isClockedIn) "CLOCKED IN" else "NOT CLOCKED IN",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatElapsed(state.elapsedSeconds),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (state.isClockedIn) "Shift in progress" else "Tap below to start your shift",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = if (state.isClockedIn) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(
                        if (state.isClockedIn) "Clock out" else "Clock in",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                state.errorMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // This week card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("This week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(weekRange, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
                Row(Modifier.fillMaxWidth()) {
                    WeekStat("Hours", String.format(Locale.UK, "%.1fh", state.weekHours), Modifier.weight(1f))
                    WeekStat("Gross", money(state.weekGross), Modifier.weight(1f))
                    WeekStat("Est. take-home", money(state.weekNet), Modifier.weight(1f), highlight = true)
                }
            }
        }
    }
}

@Composable
private fun WeekStat(label: String, value: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun money(value: Double): String = String.format(Locale.UK, "£%,.2f", value)

/** For example "9–15 Nov": Monday to Sunday of the current week. */
private fun weekRangeLabel(): String {
    val c = Calendar.getInstance()
    c.firstDayOfWeek = Calendar.MONDAY
    c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val start = c.time
    c.add(Calendar.DAY_OF_YEAR, 6)
    val end = c.time
    return SimpleDateFormat("d", Locale.UK).format(start) + "–" + SimpleDateFormat("d MMM", Locale.UK).format(end)
}

fun formatElapsed(totalSeconds: Long): String =
    "%02d:%02d:%02d".format(totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60)
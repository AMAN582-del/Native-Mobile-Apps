package com.aman.shiftmate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aman.shiftmate.model.Shift
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Stateful wrapper: owns the ViewModel
@Composable
fun HistoryScreen(
    onShiftClick: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(state = state, onShiftClick = onShiftClick, onBack = onBack, modifier = modifier)
}

// Stateless composable: receives state and callbacks
@Composable
fun HistoryContent(
    state: HistoryUiState,
    onShiftClick: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.width(16.dp))
            Text("Shift history", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))

        when {
            state.errorMessage != null ->
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            state.isLoading ->
                CircularProgressIndicator()
            state.shifts.isEmpty() ->
                Text("No shifts yet. Clock in on the Home screen to record your first one.")
            else ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.shifts, key = { it.id }) { shift ->
                        ShiftRow(shift = shift, onClick = { onShiftClick(shift.id) })
                    }
                }
        }
    }
}

// One item of the list (Topic 6: item composable)
@Composable
fun ShiftRow(shift: Shift, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(formatDate(shift.startTime), style = MaterialTheme.typography.titleMedium)
            val end = shift.endTime
            if (end == null) {
                Text("${formatTime(shift.startTime)} – in progress")
            } else {
                Text("${formatTime(shift.startTime)} – ${formatTime(end)}")
                Text("Worked: ${formatDuration(shift.startTime, end, shift.breakMinutes)}")
            }
        }
    }
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("EEE d MMM yyyy", Locale.UK).format(Date(millis))

fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.UK).format(Date(millis))

// Worked time = end - start, minus the unpaid break
fun formatDuration(start: Long, end: Long, breakMinutes: Int): String {
    val minutes = ((end - start) / 60000 - breakMinutes).coerceAtLeast(0)
    return "%dh %02dm".format(minutes / 60, minutes % 60)
}
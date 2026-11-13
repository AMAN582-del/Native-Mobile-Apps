package com.aman.shiftmate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Stateful wrapper: owns the ViewModel
@Composable
fun ShiftDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShiftDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Once the shift is deleted, go back to the list
    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    ShiftDetailContent(
        state = state,
        onBack = onBack,
        onDelete = viewModel::onDeleteClicked,
        modifier = modifier
    )
}

// Stateless composable: receives state and callbacks
@Composable
fun ShiftDetailContent(
    state: ShiftDetailUiState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.width(16.dp))
            Text("Shift details", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))

        val shift = state.shift
        when {
            state.errorMessage != null ->
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            state.isLoading || shift == null ->
                CircularProgressIndicator()
            else -> {
                DetailRow("Date", formatDate(shift.startTime))
                DetailRow("Start", formatTime(shift.startTime))
                DetailRow("End", shift.endTime?.let { formatTime(it) } ?: "In progress")
                DetailRow("Break", "${shift.breakMinutes} min")
                DetailRow("Hourly rate", "£%.2f".format(shift.hourlyRate))
                shift.endTime?.let {
                    DetailRow("Worked", formatDuration(shift.startTime, it, shift.breakMinutes))
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete shift") }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text(value)
    }
}
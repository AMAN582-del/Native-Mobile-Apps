package com.aman.shiftmate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Stateful wrapper: owns the ViewModel and collects its state
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state = state, onToggle = viewModel::onClockToggle, modifier = modifier)
}

// Stateless composable: receives state and an event callback (state hoisting)
@Composable
fun HomeContent(
    state: HomeUiState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state.isClockedIn) "You are clocked in" else "You are clocked out",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = formatElapsed(state.elapsedSeconds),
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onToggle) {
            Text(if (state.isClockedIn) "Clock Out" else "Clock In")
        }
    }
}

fun formatElapsed(totalSeconds: Long): String =
    "%02d:%02d:%02d".format(totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60)
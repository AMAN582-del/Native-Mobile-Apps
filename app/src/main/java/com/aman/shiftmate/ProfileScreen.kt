package com.aman.shiftmate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aman.shiftmate.model.TaxRegion

// Stateful wrapper: owns the ViewModel
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        state = state,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onRateChange = viewModel::onRateChange,
        onRegionChange = viewModel::onRegionChange,
        onSave = viewModel::onSave,
        modifier = modifier
    )
}

// Stateless composable: receives state and callbacks
@Composable
fun ProfileContent(
    state: ProfileUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onRegionChange: (TaxRegion) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.width(16.dp))
            Text("Profile", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.rateText,
            onValueChange = onRateChange,
            label = { Text("Hourly rate (£)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Text("Tax region", style = MaterialTheme.typography.titleMedium)
        TaxRegion.entries.forEach { region ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onRegionChange(region) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = state.taxRegion == region, onClick = { onRegionChange(region) })
                Text(region.label)
            }
        }
        Spacer(Modifier.height(24.dp))

        Button(onClick = onSave) { Text("Save") }

        if (state.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
        }
        if (state.saved) {
            Spacer(Modifier.height(12.dp))
            Text("Saved")
        }
    }
}
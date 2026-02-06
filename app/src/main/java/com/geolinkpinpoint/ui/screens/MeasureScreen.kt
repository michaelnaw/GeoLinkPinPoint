package com.geolinkpinpoint.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geolinkpinpoint.ui.MainViewModel
import com.geolinkpinpoint.ui.components.CoordinateInput
import com.geolinkpinpoint.ui.components.PointCard
import com.geolinkpinpoint.util.GeoCalculations
import com.geolinkpinpoint.util.GeoPoint

@Composable
fun MeasureScreen(viewModel: MainViewModel) {
    val state by viewModel.measureState.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    var showManualInput by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var tagText by remember { mutableStateOf("") }
    var pendingLocationTarget by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(locationState.hasLocation, pendingLocationTarget) {
        if (locationState.hasLocation && pendingLocationTarget != null) {
            val point = GeoPoint(locationState.latitude, locationState.longitude, "Current Location")
            when (pendingLocationTarget) {
                "A" -> viewModel.setPointA(point)
                "B" -> viewModel.setPointB(point)
            }
            pendingLocationTarget = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PointCard(label = "A", point = state.pointA)
        PointCard(label = "B", point = state.pointB)

        // Result card
        if (state.distanceMeters != null && state.bearingDegrees != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Distance: ${GeoCalculations.formatDistance(state.distanceMeters!!)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Bearing: %.1f\u00B0 %s".format(
                            state.bearingDegrees!!,
                            GeoCalculations.cardinalDirection(state.bearingDegrees!!)
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.pointA != null && state.pointB != null) {
                OutlinedButton(
                    onClick = { viewModel.swapPoints() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Swap")
                    Text(" Swap")
                }
            }
            if (state.distanceMeters != null) {
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                    Text(" Save")
                }
            }
        }

        // Manual input expandable
        TextButton(onClick = { showManualInput = !showManualInput }) {
            Icon(
                if (showManualInput) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle manual input"
            )
            Text(" Manual Input")
        }

        AnimatedVisibility(visible = showManualInput) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CoordinateInput(
                    label = "Point A",
                    onSubmit = { lat, lng -> viewModel.setPointA(GeoPoint(lat, lng)) },
                    onUseCurrentLocation = {
                        if (locationState.hasLocation) {
                            viewModel.setPointA(GeoPoint(locationState.latitude, locationState.longitude, "Current Location"))
                        } else {
                            pendingLocationTarget = "A"
                            permissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        }
                    }
                )
                CoordinateInput(
                    label = "Point B",
                    onSubmit = { lat, lng -> viewModel.setPointB(GeoPoint(lat, lng)) },
                    onUseCurrentLocation = {
                        if (locationState.hasLocation) {
                            viewModel.setPointB(GeoPoint(locationState.latitude, locationState.longitude, "Current Location"))
                        } else {
                            pendingLocationTarget = "B"
                            permissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        }
                    }
                )
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveDialog = false
                tagText = ""
            },
            title = { Text("Save Measurement") },
            text = {
                OutlinedTextField(
                    value = tagText,
                    onValueChange = { tagText = it },
                    label = { Text("Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveToHistory(tagText.ifBlank { null })
                    showSaveDialog = false
                    tagText = ""
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    tagText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

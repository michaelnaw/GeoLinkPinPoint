package com.geolinkpinpoint.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.geolinkpinpoint.ui.components.CompassView
import com.geolinkpinpoint.util.GeoCalculations

@Composable
fun GpsCompassScreen(viewModel: MainViewModel) {
    val locationState by viewModel.locationState.collectAsState()
    val compassState by viewModel.compassState.collectAsState()
    val measureState by viewModel.measureState.collectAsState()
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions.values.any { it }
        if (permissionGranted) {
            viewModel.startLocationUpdates()
        }
    }

    DisposableEffect(Unit) {
        viewModel.startCompass()
        onDispose {
            viewModel.stopCompass()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Compass
        Text(
            text = "Compass",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        val bearingToTarget = if (measureState.pointA != null && measureState.pointB != null) {
            measureState.bearingDegrees?.toFloat()
        } else null

        CompassView(
            azimuth = compassState.azimuth,
            bearingToTarget = bearingToTarget
        )

        Text(
            text = "%.0f\u00B0 %s".format(
                compassState.azimuth.toDouble(),
                GeoCalculations.cardinalDirection(compassState.azimuth.toDouble())
            ),
            style = MaterialTheme.typography.titleMedium
        )

        if (!compassState.isAvailable) {
            Text(
                text = "Compass sensor not available",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // GPS info
        Text(
            text = "GPS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (locationState.hasLocation) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Latitude: %.6f".format(locationState.latitude))
                    Text("Longitude: %.6f".format(locationState.longitude))
                    Text("Altitude: %.1f m".format(locationState.altitude))
                    Text("Accuracy: %.1f m".format(locationState.accuracy))
                }
            }
        } else {
            Button(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            ) {
                Text("Enable Location")
            }
            Text(
                text = "Tap to grant location permission and start GPS",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

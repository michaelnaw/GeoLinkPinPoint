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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geolinkpinpoint.R
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import com.geolinkpinpoint.ui.MainViewModel
import com.geolinkpinpoint.ui.components.CompassView
import com.geolinkpinpoint.util.GeoCalculations
import kotlinx.coroutines.launch

@Composable
fun GpsCompassScreen(viewModel: MainViewModel, snackbarHostState: SnackbarHostState) {
    val locationState by viewModel.locationState.collectAsState()
    val compassState by viewModel.compassState.collectAsState()
    val measureState by viewModel.measureState.collectAsState()
    val scope = rememberCoroutineScope()
    val permissionDeniedMsg = stringResource(R.string.location_permission_required)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.startLocationUpdates()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(permissionDeniedMsg)
            }
        }
    }

    DisposableEffect(Unit) {
        viewModel.startCompass()
        onDispose {
            viewModel.stopCompass()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationUpdates()
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
            text = stringResource(R.string.compass_title),
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
                text = stringResource(R.string.compass_not_available),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // GPS info
        Text(
            text = stringResource(R.string.gps_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        when {
            locationState.hasLocation -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.latitude_value).format(locationState.latitude))
                        Text(stringResource(R.string.longitude_value).format(locationState.longitude))
                        Text(stringResource(R.string.altitude_value).format(locationState.altitude))
                        Text(stringResource(R.string.accuracy_value).format(locationState.accuracy))
                    }
                }
            }
            locationState.isAcquiringLocation -> {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Text(
                    text = stringResource(R.string.acquiring_location),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
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
                    Text(stringResource(R.string.enable_location))
                }
                Text(
                    text = stringResource(R.string.enable_location_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

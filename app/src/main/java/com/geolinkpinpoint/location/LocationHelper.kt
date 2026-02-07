package com.geolinkpinpoint.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val hasLocation: Boolean = false
)

class LocationHelper(context: Context) : DefaultLifecycleObserver {

    private companion object {
        private const val TAG = "LocationHelper"
    }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private var updatesRequested = false
    private var boundLifecycle: Lifecycle? = null

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 1000L
    ).setMinUpdateIntervalMillis(500L).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                _locationState.value = LocationState(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    accuracy = location.accuracy,
                    hasLocation = true
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startUpdates() {
        updatesRequested = true
        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            updatesRequested = false
            _locationState.value = LocationState()
            Log.w(TAG, "Location permission revoked, cannot start updates", e)
        }
    }

    fun stopUpdates() {
        updatesRequested = false
        fusedClient.removeLocationUpdates(locationCallback)
        _locationState.value = LocationState()
    }

    fun bindToLifecycle(lifecycle: Lifecycle) {
        if (boundLifecycle === lifecycle) return
        boundLifecycle?.removeObserver(this)
        boundLifecycle = lifecycle
        lifecycle.addObserver(this)
    }

    @SuppressLint("MissingPermission")
    override fun onResume(owner: LifecycleOwner) {
        if (updatesRequested) {
            try {
                fusedClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                updatesRequested = false
                _locationState.value = LocationState()
                Log.w(TAG, "Location permission revoked, cannot resume updates", e)
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (updatesRequested) {
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }
}

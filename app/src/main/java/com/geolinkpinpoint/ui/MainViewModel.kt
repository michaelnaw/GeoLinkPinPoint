package com.geolinkpinpoint.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geolinkpinpoint.data.MeasurementEntity
import com.geolinkpinpoint.data.MeasurementRepository
import com.geolinkpinpoint.location.LocationHelper
import com.geolinkpinpoint.location.LocationState
import com.geolinkpinpoint.sensor.CompassHelper
import com.geolinkpinpoint.sensor.CompassState
import com.geolinkpinpoint.util.GeoCalculations
import com.geolinkpinpoint.util.GeoPoint
import com.geolinkpinpoint.util.GeoUriParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MeasureState(
    val pointA: GeoPoint? = null,
    val pointB: GeoPoint? = null,
    val distanceMeters: Double? = null,
    val bearingDegrees: Double? = null
)

class MainViewModel(
    application: Application,
    private val repository: MeasurementRepository
) : AndroidViewModel(application) {

    val locationHelper = LocationHelper(application)
    val compassHelper = CompassHelper(application)

    private val _measureState = MutableStateFlow(MeasureState())
    val measureState: StateFlow<MeasureState> = _measureState.asStateFlow()

    val locationState: StateFlow<LocationState> = locationHelper.locationState
    val compassState: StateFlow<CompassState> = compassHelper.compassState

    val measurements = repository.getAllMeasurements()

    fun handleGeoUri(uri: String) {
        val point = GeoUriParser.parse(uri) ?: return
        val current = _measureState.value
        if (current.pointA == null) {
            setPointA(point)
        } else {
            setPointB(point)
        }
    }

    fun setPointA(point: GeoPoint) {
        val current = _measureState.value.copy(pointA = point)
        _measureState.value = recalculate(current)
    }

    fun setPointB(point: GeoPoint) {
        val current = _measureState.value.copy(pointB = point)
        _measureState.value = recalculate(current)
    }

    fun clearPoints() {
        _measureState.value = MeasureState()
    }

    fun swapPoints() {
        val current = _measureState.value
        val swapped = current.copy(pointA = current.pointB, pointB = current.pointA)
        _measureState.value = recalculate(swapped)
    }

    fun saveToHistory(tag: String? = null) {
        val state = _measureState.value
        val a = state.pointA ?: return
        val b = state.pointB ?: return
        val dist = state.distanceMeters ?: return
        val bearing = state.bearingDegrees ?: return

        viewModelScope.launch {
            repository.insert(
                MeasurementEntity(
                    pointALatitude = a.latitude,
                    pointALongitude = a.longitude,
                    pointALabel = a.label,
                    pointBLatitude = b.latitude,
                    pointBLongitude = b.longitude,
                    pointBLabel = b.label,
                    distanceMeters = dist,
                    bearingDegrees = bearing,
                    tag = tag
                )
            )
        }
    }

    fun loadFromHistory(measurement: MeasurementEntity) {
        val pointA = GeoPoint(measurement.pointALatitude, measurement.pointALongitude, measurement.pointALabel)
        val pointB = GeoPoint(measurement.pointBLatitude, measurement.pointBLongitude, measurement.pointBLabel)
        _measureState.value = MeasureState(
            pointA = pointA,
            pointB = pointB,
            distanceMeters = measurement.distanceMeters,
            bearingDegrees = measurement.bearingDegrees
        )
    }

    fun deleteFromHistory(measurement: MeasurementEntity) {
        viewModelScope.launch {
            repository.delete(measurement)
        }
    }

    fun startLocationUpdates() {
        locationHelper.startUpdates()
    }

    fun stopLocationUpdates() {
        locationHelper.stopUpdates()
    }

    fun startCompass() {
        compassHelper.start()
    }

    fun stopCompass() {
        compassHelper.stop()
    }

    suspend fun exportMeasurementsCsv(): Uri? {
        return try {
            val items = repository.getAllMeasurementsOnce()
            if (items.isEmpty()) return null

            val app = getApplication<Application>()
            val exportDir = File(app.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "measurements.csv")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

            file.bufferedWriter().use { writer ->
                writer.write("Tag,PointA_Lat,PointA_Lng,PointA_Label,PointB_Lat,PointB_Lng,PointB_Label,Distance_m,Bearing_deg,Timestamp")
                writer.newLine()
                for (m in items) {
                    val fields = listOf(
                        escapeCsv(m.tag ?: ""),
                        m.pointALatitude.toString(),
                        m.pointALongitude.toString(),
                        escapeCsv(m.pointALabel ?: ""),
                        m.pointBLatitude.toString(),
                        m.pointBLongitude.toString(),
                        escapeCsv(m.pointBLabel ?: ""),
                        "%.2f".format(m.distanceMeters),
                        "%.2f".format(m.bearingDegrees),
                        dateFormat.format(Date(m.timestamp))
                    )
                    writer.write(fields.joinToString(","))
                    writer.newLine()
                }
            }

            FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Log.e("MainViewModel", "CSV export failed", e)
            null
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun recalculate(state: MeasureState): MeasureState {
        val a = state.pointA
        val b = state.pointB
        if (a == null || b == null) {
            return state.copy(distanceMeters = null, bearingDegrees = null)
        }
        val distance = GeoCalculations.haversineDistance(a.latitude, a.longitude, b.latitude, b.longitude)
        val bearing = GeoCalculations.forwardBearing(a.latitude, a.longitude, b.latitude, b.longitude)
        return state.copy(distanceMeters = distance, bearingDegrees = bearing)
    }
}

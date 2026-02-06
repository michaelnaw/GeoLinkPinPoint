package com.geolinkpinpoint.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CompassState(
    val azimuth: Float = 0f,
    val isAvailable: Boolean = false
)

class CompassHelper(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassState = MutableStateFlow(CompassState())
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var currentAzimuth = 0f
    private var lastEmitTimeNanos = 0L

    private val alpha = 0.15f

    private companion object {
        const val THROTTLE_INTERVAL_NS = 50_000_000L // 50ms
    }

    val isAvailable: Boolean
        get() = accelerometer != null && magnetometer != null

    fun start() {
        if (accelerometer != null && magnetometer != null) {
            lastEmitTimeNanos = 0L
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            _compassState.value = _compassState.value.copy(isAvailable = true)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = lowPass(event.values, gravity)
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = lowPass(event.values, geomagnetic)
        }

        val g = gravity ?: return
        val m = geomagnetic ?: return

        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)

        if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, g, m)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthDegrees = ((Math.toDegrees(orientation[0].toDouble()).toFloat()) + 360) % 360
            currentAzimuth = azimuthDegrees

            val now = System.nanoTime()
            if (now - lastEmitTimeNanos >= THROTTLE_INTERVAL_NS) {
                lastEmitTimeNanos = now
                _compassState.value = CompassState(azimuth = currentAzimuth, isAvailable = true)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input.copyOf()
        return FloatArray(input.size) { i ->
            output[i] + alpha * (input[i] - output[i])
        }
    }
}

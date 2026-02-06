package com.geolinkpinpoint.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMeasurementDao : MeasurementDao {
    private val measurements = MutableStateFlow<List<MeasurementEntity>>(emptyList())
    private var autoId = 1L

    override fun getAllMeasurements(): Flow<List<MeasurementEntity>> =
        measurements.map { list -> list.sortedByDescending { it.timestamp } }

    override suspend fun getAllMeasurementsOnce(): List<MeasurementEntity> =
        measurements.value.sortedByDescending { it.timestamp }

    override suspend fun insert(measurement: MeasurementEntity) {
        val withId = if (measurement.id == 0L) {
            measurement.copy(id = autoId++)
        } else {
            measurement
        }
        measurements.value = measurements.value + withId
    }

    override suspend fun delete(measurement: MeasurementEntity) {
        measurements.value = measurements.value.filter { it.id != measurement.id }
    }
}

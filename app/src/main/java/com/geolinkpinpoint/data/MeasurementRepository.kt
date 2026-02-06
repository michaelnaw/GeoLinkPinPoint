package com.geolinkpinpoint.data

import kotlinx.coroutines.flow.Flow

class MeasurementRepository(private val dao: MeasurementDao) {

    fun getAllMeasurements(): Flow<List<MeasurementEntity>> =
        dao.getAllMeasurements()

    suspend fun getAllMeasurementsOnce(): List<MeasurementEntity> =
        dao.getAllMeasurementsOnce()

    suspend fun insert(measurement: MeasurementEntity) =
        dao.insert(measurement)

    suspend fun delete(measurement: MeasurementEntity) =
        dao.delete(measurement)
}

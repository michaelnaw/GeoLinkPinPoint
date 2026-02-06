package com.geolinkpinpoint.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MeasurementRepositoryTest {

    private lateinit var fakeDao: FakeMeasurementDao
    private lateinit var repository: MeasurementRepository

    @Before
    fun setup() {
        fakeDao = FakeMeasurementDao()
        repository = MeasurementRepository(fakeDao)
    }

    private fun createEntity(
        latA: Double = 32.0, lngA: Double = 34.0,
        latB: Double = 33.0, lngB: Double = 35.0,
        tag: String? = null
    ) = MeasurementEntity(
        pointALatitude = latA, pointALongitude = lngA, pointALabel = null,
        pointBLatitude = latB, pointBLongitude = lngB, pointBLabel = null,
        distanceMeters = 100.0, bearingDegrees = 45.0, tag = tag
    )

    @Test
    fun `insert and retrieve measurement`() = runTest {
        val entity = createEntity(tag = "Test")
        repository.insert(entity)

        val result = repository.getAllMeasurementsOnce()
        assertEquals(1, result.size)
        assertEquals("Test", result[0].tag)
    }

    @Test
    fun `getAllMeasurements flow emits after insert`() = runTest {
        repository.insert(createEntity(tag = "First"))

        val list = repository.getAllMeasurements().first()
        assertEquals(1, list.size)
        assertEquals("First", list[0].tag)
    }

    @Test
    fun `delete removes measurement`() = runTest {
        repository.insert(createEntity(tag = "ToDelete"))
        val inserted = repository.getAllMeasurementsOnce()
        assertEquals(1, inserted.size)

        repository.delete(inserted[0])
        val afterDelete = repository.getAllMeasurementsOnce()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun `multiple inserts all persist`() = runTest {
        repository.insert(createEntity(tag = "First"))
        repository.insert(createEntity(tag = "Second"))
        repository.insert(createEntity(tag = "Third"))

        val result = repository.getAllMeasurementsOnce()
        assertEquals(3, result.size)
    }

    @Test
    fun `insert assigns auto-generated ids`() = runTest {
        repository.insert(createEntity(tag = "A"))
        repository.insert(createEntity(tag = "B"))

        val result = repository.getAllMeasurementsOnce()
        assert(result[0].id != result[1].id) { "IDs should be unique" }
    }

    @Test
    fun `delete only removes specified measurement`() = runTest {
        repository.insert(createEntity(tag = "Keep"))
        repository.insert(createEntity(tag = "Remove"))

        val all = repository.getAllMeasurementsOnce()
        val toRemove = all.first { it.tag == "Remove" }
        repository.delete(toRemove)

        val remaining = repository.getAllMeasurementsOnce()
        assertEquals(1, remaining.size)
        assertEquals("Keep", remaining[0].tag)
    }

    @Test
    fun `entity preserves all fields`() = runTest {
        val entity = MeasurementEntity(
            pointALatitude = 32.0853,
            pointALongitude = 34.7818,
            pointALabel = "Tel Aviv",
            pointBLatitude = 32.7940,
            pointBLongitude = 34.9896,
            pointBLabel = "Haifa",
            distanceMeters = 82000.0,
            bearingDegrees = 16.5,
            tag = "Route 1"
        )
        repository.insert(entity)

        val result = repository.getAllMeasurementsOnce()[0]
        assertEquals(32.0853, result.pointALatitude, 0.0001)
        assertEquals(34.7818, result.pointALongitude, 0.0001)
        assertEquals("Tel Aviv", result.pointALabel)
        assertEquals(32.7940, result.pointBLatitude, 0.0001)
        assertEquals(34.9896, result.pointBLongitude, 0.0001)
        assertEquals("Haifa", result.pointBLabel)
        assertEquals(82000.0, result.distanceMeters, 0.1)
        assertEquals(16.5, result.bearingDegrees, 0.1)
        assertEquals("Route 1", result.tag)
    }

    @Test
    fun `entity with null labels and tag`() = runTest {
        repository.insert(createEntity(tag = null))

        val result = repository.getAllMeasurementsOnce()[0]
        assertNull(result.pointALabel)
        assertNull(result.pointBLabel)
        assertNull(result.tag)
    }
}

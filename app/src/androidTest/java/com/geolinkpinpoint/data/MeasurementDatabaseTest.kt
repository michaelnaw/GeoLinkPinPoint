package com.geolinkpinpoint.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeasurementDatabaseTest {

    private lateinit var database: MeasurementDatabase
    private lateinit var dao: MeasurementDao

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MeasurementDatabase::class.java
    )

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MeasurementDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.measurementDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createEntity(tag: String? = null) = MeasurementEntity(
        pointALatitude = 32.0853, pointALongitude = 34.7818, pointALabel = "Tel Aviv",
        pointBLatitude = 32.7940, pointBLongitude = 34.9896, pointBLabel = "Haifa",
        distanceMeters = 82000.0, bearingDegrees = 16.5, tag = tag
    )

    @Test
    fun insertAndQuery() = runTest {
        dao.insert(createEntity(tag = "Test Route"))
        val results = dao.getAllMeasurementsOnce()
        assertEquals(1, results.size)
        assertEquals("Test Route", results[0].tag)
        assertEquals(32.0853, results[0].pointALatitude, 0.0001)
    }

    @Test
    fun insertAndQueryFlow() = runTest {
        dao.insert(createEntity())
        val results = dao.getAllMeasurements().first()
        assertEquals(1, results.size)
    }

    @Test
    fun deleteRemovesEntry() = runTest {
        dao.insert(createEntity())
        val inserted = dao.getAllMeasurementsOnce()
        dao.delete(inserted[0])
        val afterDelete = dao.getAllMeasurementsOnce()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun autoGenerateIdIncrementsCorrectly() = runTest {
        dao.insert(createEntity(tag = "First"))
        dao.insert(createEntity(tag = "Second"))
        val results = dao.getAllMeasurementsOnce()
        assertEquals(2, results.size)
        assertNotEquals(results[0].id, results[1].id)
    }

    @Test
    fun resultsOrderedByTimestampDescending() = runTest {
        dao.insert(createEntity(tag = "Older").copy(timestamp = 1000L))
        dao.insert(createEntity(tag = "Newer").copy(timestamp = 2000L))
        val results = dao.getAllMeasurementsOnce()
        assertEquals("Newer", results[0].tag)
        assertEquals("Older", results[1].tag)
    }

    @Test
    fun nullableFieldsStoredCorrectly() = runTest {
        dao.insert(MeasurementEntity(
            pointALatitude = 0.0, pointALongitude = 0.0, pointALabel = null,
            pointBLatitude = 1.0, pointBLongitude = 1.0, pointBLabel = null,
            distanceMeters = 157000.0, bearingDegrees = 45.0, tag = null
        ))
        val result = dao.getAllMeasurementsOnce()[0]
        assertNull(result.pointALabel)
        assertNull(result.pointBLabel)
        assertNull(result.tag)
    }

    // --- Migration test ---

    @Test
    fun migration1To2AddsTagColumn() {
        val dbName = "migration-test"
        migrationTestHelper.createDatabase(dbName, 1).apply {
            execSQL("""
                INSERT INTO measurements
                (pointALatitude, pointALongitude, pointALabel,
                 pointBLatitude, pointBLongitude, pointBLabel,
                 distanceMeters, bearingDegrees, timestamp)
                VALUES (32.0, 34.0, 'A', 33.0, 35.0, 'B', 100.0, 45.0, 1000)
            """)
            close()
        }

        val db = migrationTestHelper.runMigrationsAndValidate(
            dbName, 2, true, MeasurementDatabase.MIGRATION_1_2
        )

        val cursor = db.query("SELECT tag FROM measurements")
        assertTrue(cursor.moveToFirst())
        assertTrue(cursor.isNull(0))
        cursor.close()
        db.close()
    }
}

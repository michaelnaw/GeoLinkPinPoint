package com.geolinkpinpoint.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoCalculationsTest {

    // --- Haversine distance with known city pairs ---

    @Test
    fun `distance Tel Aviv to Haifa is approximately 82 km`() {
        val distance = GeoCalculations.haversineDistance(
            32.0853, 34.7818,  // Tel Aviv
            32.7940, 34.9896   // Haifa
        )
        assertEquals(82_000.0, distance, 2_000.0)
    }

    @Test
    fun `distance London to Paris is approximately 344 km`() {
        val distance = GeoCalculations.haversineDistance(
            51.5074, -0.1278,  // London
            48.8566, 2.3522    // Paris
        )
        assertEquals(344_000.0, distance, 5_000.0)
    }

    @Test
    fun `distance New York to Los Angeles is approximately 3944 km`() {
        val distance = GeoCalculations.haversineDistance(
            40.7128, -74.0060,   // New York
            34.0522, -118.2437   // Los Angeles
        )
        assertEquals(3_944_000.0, distance, 20_000.0)
    }

    @Test
    fun `distance from point to itself is zero`() {
        val distance = GeoCalculations.haversineDistance(
            32.0853, 34.7818,
            32.0853, 34.7818
        )
        assertEquals(0.0, distance, 0.01)
    }

    @Test
    fun `distance across antimeridian`() {
        val distance = GeoCalculations.haversineDistance(
            0.0, 179.0,
            0.0, -179.0
        )
        // 2 degrees at equator ≈ 222 km
        assertEquals(222_000.0, distance, 5_000.0)
    }

    @Test
    fun `distance is symmetric`() {
        val d1 = GeoCalculations.haversineDistance(32.0853, 34.7818, 32.7940, 34.9896)
        val d2 = GeoCalculations.haversineDistance(32.7940, 34.9896, 32.0853, 34.7818)
        assertEquals(d1, d2, 0.01)
    }

    @Test
    fun `distance at poles`() {
        // North pole to south pole ≈ 20015 km (half circumference)
        val distance = GeoCalculations.haversineDistance(90.0, 0.0, -90.0, 0.0)
        assertEquals(20_015_000.0, distance, 100_000.0)
    }

    // --- Forward bearing ---

    @Test
    fun `bearing due north is approximately 0 degrees`() {
        val bearing = GeoCalculations.forwardBearing(0.0, 0.0, 1.0, 0.0)
        assertEquals(0.0, bearing, 1.0)
    }

    @Test
    fun `bearing due east is approximately 90 degrees`() {
        val bearing = GeoCalculations.forwardBearing(0.0, 0.0, 0.0, 1.0)
        assertEquals(90.0, bearing, 1.0)
    }

    @Test
    fun `bearing due south is approximately 180 degrees`() {
        val bearing = GeoCalculations.forwardBearing(1.0, 0.0, 0.0, 0.0)
        assertEquals(180.0, bearing, 1.0)
    }

    @Test
    fun `bearing due west is approximately 270 degrees`() {
        val bearing = GeoCalculations.forwardBearing(0.0, 1.0, 0.0, 0.0)
        assertEquals(270.0, bearing, 1.0)
    }

    @Test
    fun `bearing from same point is 0`() {
        val bearing = GeoCalculations.forwardBearing(32.0853, 34.7818, 32.0853, 34.7818)
        assertEquals(0.0, bearing, 0.01)
    }

    @Test
    fun `bearing is always in 0 to 360 range`() {
        val bearing = GeoCalculations.forwardBearing(0.0, 1.0, 0.0, 0.0)
        assertTrue("Bearing $bearing should be >= 0", bearing >= 0.0)
        assertTrue("Bearing $bearing should be < 360", bearing < 360.0)
    }

    @Test
    fun `bearing Tel Aviv to Haifa is roughly NNE`() {
        val bearing = GeoCalculations.forwardBearing(
            32.0853, 34.7818,
            32.7940, 34.9896
        )
        assertTrue("Bearing should be between 0 and 45, was $bearing", bearing in 0.0..45.0)
    }

    // --- Cardinal direction ---

    @Test
    fun `cardinal direction for 0 degrees is N`() {
        assertEquals("N", GeoCalculations.cardinalDirection(0.0))
    }

    @Test
    fun `cardinal direction for 359 degrees is N`() {
        assertEquals("N", GeoCalculations.cardinalDirection(359.0))
    }

    @Test
    fun `cardinal direction for 22 degrees is N`() {
        assertEquals("N", GeoCalculations.cardinalDirection(22.0))
    }

    @Test
    fun `cardinal direction for 45 degrees is NE`() {
        assertEquals("NE", GeoCalculations.cardinalDirection(45.0))
    }

    @Test
    fun `cardinal direction for 90 degrees is E`() {
        assertEquals("E", GeoCalculations.cardinalDirection(90.0))
    }

    @Test
    fun `cardinal direction for 135 degrees is SE`() {
        assertEquals("SE", GeoCalculations.cardinalDirection(135.0))
    }

    @Test
    fun `cardinal direction for 180 degrees is S`() {
        assertEquals("S", GeoCalculations.cardinalDirection(180.0))
    }

    @Test
    fun `cardinal direction for 225 degrees is SW`() {
        assertEquals("SW", GeoCalculations.cardinalDirection(225.0))
    }

    @Test
    fun `cardinal direction for 270 degrees is W`() {
        assertEquals("W", GeoCalculations.cardinalDirection(270.0))
    }

    @Test
    fun `cardinal direction for 315 degrees is NW`() {
        assertEquals("NW", GeoCalculations.cardinalDirection(315.0))
    }

    @Test
    fun `cardinal direction handles negative input`() {
        assertEquals("W", GeoCalculations.cardinalDirection(-90.0))
    }

    @Test
    fun `cardinal direction handles values over 360`() {
        assertEquals("E", GeoCalculations.cardinalDirection(450.0))
    }

    @Test
    fun `cardinal direction boundary at 337_5 is N`() {
        assertEquals("N", GeoCalculations.cardinalDirection(337.5))
    }

    // --- formatDistance ---

    @Test
    fun `formatDistance below 1000m shows meters`() {
        assertEquals("500.0 m", GeoCalculations.formatDistance(500.0))
    }

    @Test
    fun `formatDistance at exactly 1000m shows km`() {
        assertEquals("1.00 km", GeoCalculations.formatDistance(1000.0))
    }

    @Test
    fun `formatDistance above 1000m shows km with two decimals`() {
        assertEquals("3.50 km", GeoCalculations.formatDistance(3500.0))
    }

    @Test
    fun `formatDistance very small value`() {
        assertEquals("0.5 m", GeoCalculations.formatDistance(0.5))
    }

    @Test
    fun `formatDistance zero`() {
        assertEquals("0.0 m", GeoCalculations.formatDistance(0.0))
    }

    @Test
    fun `formatDistance just below threshold`() {
        assertEquals("999.9 m", GeoCalculations.formatDistance(999.9))
    }
}

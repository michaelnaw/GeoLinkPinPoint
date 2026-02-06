package com.geolinkpinpoint.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GeoUriParserTest {

    // --- Basic coordinate parsing ---

    @Test
    fun `parse simple geo URI returns correct coordinates`() {
        val result = GeoUriParser.parse("geo:32.0853,34.7818")
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.0001)
        assertEquals(34.7818, result.longitude, 0.0001)
        assertNull(result.label)
    }

    @Test
    fun `parse geo URI with negative coordinates`() {
        val result = GeoUriParser.parse("geo:-33.8688,151.2093")
        assertNotNull(result)
        assertEquals(-33.8688, result!!.latitude, 0.0001)
        assertEquals(151.2093, result.longitude, 0.0001)
    }

    @Test
    fun `parse geo URI with both negative coordinates`() {
        val result = GeoUriParser.parse("geo:-22.9068,-43.1729")
        assertNotNull(result)
        assertEquals(-22.9068, result!!.latitude, 0.0001)
        assertEquals(-43.1729, result.longitude, 0.0001)
    }

    @Test
    fun `parse high precision coordinates`() {
        val result = GeoUriParser.parse("geo:32.08530000,34.78180000")
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.00001)
        assertEquals(34.7818, result.longitude, 0.00001)
    }

    // --- 0,0 sentinel with ?q= parameter ---

    @Test
    fun `parse 0,0 sentinel with q param extracts coords and label`() {
        val result = GeoUriParser.parse("geo:0,0?q=32.7940,34.9896(Haifa)")
        assertNotNull(result)
        assertEquals(32.7940, result!!.latitude, 0.0001)
        assertEquals(34.9896, result.longitude, 0.0001)
        assertEquals("Haifa", result.label)
    }

    @Test
    fun `parse 0,0 sentinel with q param without label`() {
        val result = GeoUriParser.parse("geo:0,0?q=32.7940,34.9896")
        assertNotNull(result)
        assertEquals(32.7940, result!!.latitude, 0.0001)
        assertEquals(34.9896, result.longitude, 0.0001)
        assertNull(result.label)
    }

    @Test
    fun `parse 0,0 sentinel with URL encoded q param`() {
        val result = GeoUriParser.parse("geo:0,0?q=32.7940%2C34.9896%28Haifa%29")
        assertNotNull(result)
        assertEquals(32.7940, result!!.latitude, 0.0001)
        assertEquals(34.9896, result.longitude, 0.0001)
        assertEquals("Haifa", result.label)
    }

    @Test
    fun `parse 0,0 sentinel with label containing spaces`() {
        val result = GeoUriParser.parse("geo:0,0?q=32.0853,34.7818(Ben Gurion Airport)")
        assertNotNull(result)
        assertEquals("Ben Gurion Airport", result!!.label)
    }

    // --- Label extraction from non-0,0 URIs ---

    @Test
    fun `parse non-zero URI with q label extracts label`() {
        val result = GeoUriParser.parse("geo:32.0853,34.7818?q=32.0853,34.7818(Tel Aviv)")
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.0001)
        assertEquals(34.7818, result.longitude, 0.0001)
        assertEquals("Tel Aviv", result.label)
    }

    @Test
    fun `parse non-zero URI without q param has no label`() {
        val result = GeoUriParser.parse("geo:32.0853,34.7818")
        assertNotNull(result)
        assertNull(result!!.label)
    }

    // --- Geo URI parameters (;crs=, ;u=) ---

    @Test
    fun `parse URI with semicolon uncertainty parameter`() {
        val result = GeoUriParser.parse("geo:32.0853,34.7818;u=35")
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.0001)
        assertEquals(34.7818, result.longitude, 0.0001)
    }

    @Test
    fun `parse URI with semicolon crs parameter`() {
        val result = GeoUriParser.parse("geo:32.0853,34.7818;crs=wgs84")
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.0001)
    }

    // --- Edge cases and invalid input ---

    @Test
    fun `parse non-geo URI returns null`() {
        assertNull(GeoUriParser.parse("https://maps.google.com"))
    }

    @Test
    fun `parse empty string returns null`() {
        assertNull(GeoUriParser.parse(""))
    }

    @Test
    fun `parse geo with non-numeric coords returns null`() {
        assertNull(GeoUriParser.parse("geo:abc,def"))
    }

    @Test
    fun `parse geo with missing longitude returns null`() {
        assertNull(GeoUriParser.parse("geo:32.0853"))
    }

    @Test
    fun `parse geo with only scheme returns null`() {
        assertNull(GeoUriParser.parse("geo:"))
    }

    @Test
    fun `parse actual 0,0 coordinates without query returns GeoPoint at origin`() {
        val result = GeoUriParser.parse("geo:0,0")
        assertNotNull(result)
        assertEquals(0.0, result!!.latitude, 0.0001)
        assertEquals(0.0, result.longitude, 0.0001)
    }

    @Test
    fun `parse 0,0 with non-q query params returns null`() {
        // Documents current behavior: 0,0 with query but no q= param returns null
        // because sentinel logic delegates to parseQueryParam which finds no q= param
        assertNull(GeoUriParser.parse("geo:0,0?z=5"))
    }

    @Test
    fun `parse coords with spaces trims correctly`() {
        val result = GeoUriParser.parse("geo: 32.0853 , 34.7818 ")
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.0001)
        assertEquals(34.7818, result.longitude, 0.0001)
    }

    @Test
    fun `parse URI with multiple query params finds q param`() {
        val result = GeoUriParser.parse("geo:0,0?z=5&q=32.0853,34.7818(Tel Aviv)")
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.0001)
        assertEquals("Tel Aviv", result.label)
    }

    @Test
    fun `parse 0,0 sentinel with altitude in coords part`() {
        // geo:0,0,0 (with altitude) — split on comma gives 3 parts, still works
        val result = GeoUriParser.parse("geo:0,0,0?q=32.0853,34.7818")
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.0001)
    }

    // --- Coordinate range validation ---

    @Test
    fun `parse latitude above 90 returns null`() {
        assertNull(GeoUriParser.parse("geo:91.0,34.7818"))
    }

    @Test
    fun `parse latitude below negative 90 returns null`() {
        assertNull(GeoUriParser.parse("geo:-91.0,34.7818"))
    }

    @Test
    fun `parse longitude above 180 returns null`() {
        assertNull(GeoUriParser.parse("geo:32.0853,181.0"))
    }

    @Test
    fun `parse longitude below negative 180 returns null`() {
        assertNull(GeoUriParser.parse("geo:32.0853,-181.0"))
    }

    @Test
    fun `parse exact boundary latitude 90 is valid`() {
        val result = GeoUriParser.parse("geo:90.0,0.0")
        assertNotNull(result)
        assertEquals(90.0, result!!.latitude, 0.0001)
    }

    @Test
    fun `parse exact boundary latitude negative 90 is valid`() {
        val result = GeoUriParser.parse("geo:-90.0,0.0")
        assertNotNull(result)
        assertEquals(-90.0, result!!.latitude, 0.0001)
    }

    @Test
    fun `parse exact boundary longitude 180 is valid`() {
        val result = GeoUriParser.parse("geo:0.0,180.0")
        assertNotNull(result)
        assertEquals(180.0, result!!.longitude, 0.0001)
    }

    @Test
    fun `parse exact boundary longitude negative 180 is valid`() {
        val result = GeoUriParser.parse("geo:0.0,-180.0")
        assertNotNull(result)
        assertEquals(-180.0, result!!.longitude, 0.0001)
    }

    @Test
    fun `parse out-of-range coords in q parameter with label returns null`() {
        assertNull(GeoUriParser.parse("geo:0,0?q=91.0,34.7818(Invalid)"))
    }

    @Test
    fun `parse out-of-range longitude in q parameter returns null`() {
        assertNull(GeoUriParser.parse("geo:0,0?q=32.0,200.0"))
    }
}

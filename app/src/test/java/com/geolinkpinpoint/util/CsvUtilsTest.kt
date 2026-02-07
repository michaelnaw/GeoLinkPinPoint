package com.geolinkpinpoint.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvUtilsTest {

    // --- Plain values pass through unchanged ---

    @Test
    fun `plain text passes through unchanged`() {
        assertEquals("Hello", CsvUtils.escapeCsv("Hello"))
    }

    @Test
    fun `empty string passes through unchanged`() {
        assertEquals("", CsvUtils.escapeCsv(""))
    }

    @Test
    fun `numeric string passes through unchanged`() {
        assertEquals("42.5", CsvUtils.escapeCsv("42.5"))
    }

    // --- Standard CSV quoting ---

    @Test
    fun `value containing comma is quoted`() {
        assertEquals("\"Hello, World\"", CsvUtils.escapeCsv("Hello, World"))
    }

    @Test
    fun `value containing newline is quoted`() {
        assertEquals("\"Line1\nLine2\"", CsvUtils.escapeCsv("Line1\nLine2"))
    }

    @Test
    fun `value containing tab is quoted`() {
        assertEquals("\"Col1\tCol2\"", CsvUtils.escapeCsv("Col1\tCol2"))
    }

    @Test
    fun `value containing carriage return is quoted`() {
        assertEquals("\"Line1\rLine2\"", CsvUtils.escapeCsv("Line1\rLine2"))
    }

    @Test
    fun `value containing double quote is escaped and quoted`() {
        assertEquals("\"She said \"\"hi\"\"\"", CsvUtils.escapeCsv("She said \"hi\""))
    }

    // --- Formula injection prevention: direct prefix ---

    @Test
    fun `value starting with equals is neutralised`() {
        assertEquals("\"'=SUM(A1:A10)\"", CsvUtils.escapeCsv("=SUM(A1:A10)"))
    }

    @Test
    fun `value starting with plus is neutralised`() {
        assertEquals("\"'+cmd\"", CsvUtils.escapeCsv("+cmd"))
    }

    @Test
    fun `value starting with minus is neutralised`() {
        assertEquals("\"'-1+1\"", CsvUtils.escapeCsv("-1+1"))
    }

    @Test
    fun `value starting with at sign is neutralised`() {
        assertEquals("\"'@SUM(A1)\"", CsvUtils.escapeCsv("@SUM(A1)"))
    }

    // --- Formula injection prevention: whitespace bypass ---

    @Test
    fun `value with leading space before equals is neutralised`() {
        assertEquals("\"' =SUM(A1)\"", CsvUtils.escapeCsv(" =SUM(A1)"))
    }

    @Test
    fun `value with leading tab before equals is neutralised`() {
        assertEquals("\"'\t=SUM(A1)\"", CsvUtils.escapeCsv("\t=SUM(A1)"))
    }

    @Test
    fun `value with multiple leading spaces before plus is neutralised`() {
        assertEquals("\"'   +cmd\"", CsvUtils.escapeCsv("   +cmd"))
    }

    @Test
    fun `value with leading space before at sign is neutralised`() {
        assertEquals("\"' @SUM(A1)\"", CsvUtils.escapeCsv(" @SUM(A1)"))
    }

    @Test
    fun `value with leading space before minus is neutralised`() {
        assertEquals("\"' -1+1\"", CsvUtils.escapeCsv(" -1+1"))
    }

    // --- Edge cases ---

    @Test
    fun `value that is only whitespace passes through`() {
        assertEquals("   ", CsvUtils.escapeCsv("   "))
    }

    @Test
    fun `formula char with quotes is both escaped and neutralised`() {
        assertEquals("\"'=She said \"\"hi\"\"\"", CsvUtils.escapeCsv("=She said \"hi\""))
    }

    @Test
    fun `single minus sign is neutralised`() {
        assertEquals("\"'-\"", CsvUtils.escapeCsv("-"))
    }

    @Test
    fun `single equals sign is neutralised`() {
        assertEquals("\"'=\"", CsvUtils.escapeCsv("="))
    }
}

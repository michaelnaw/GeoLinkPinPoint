package com.geolinkpinpoint.util

object CsvUtils {

    /**
     * Escapes a value for safe inclusion in a CSV cell.
     *
     * Dangerous formula prefixes (=, +, -, @) are neutralised by prepending
     * a single-quote inside double-quotes.  The value is trimmed before
     * checking so that leading whitespace cannot bypass the guard.
     */
    fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        val trimmed = escaped.trimStart()
        return when {
            trimmed.isNotEmpty() && trimmed[0] in charArrayOf('=', '+', '-', '@') ->
                "\"'$escaped\""
            escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\t") ->
                "\"$escaped\""
            else -> escaped
        }
    }
}

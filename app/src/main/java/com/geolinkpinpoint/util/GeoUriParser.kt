package com.geolinkpinpoint.util

import java.net.URLDecoder

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val label: String? = null
)

object GeoUriParser {

    fun parse(uri: String): GeoPoint? {
        if (!uri.startsWith("geo:")) return null

        val withoutScheme = uri.removePrefix("geo:")
        val mainAndQuery = withoutScheme.split("?", limit = 2)
        val coordsPart = mainAndQuery[0]
        val queryPart = mainAndQuery.getOrNull(1)

        val coords = parseCoords(coordsPart)
        if (coords == null) return null

        // If coords are 0,0 and there's a query param, try parsing from q=
        if (coords.first == 0.0 && coords.second == 0.0 && queryPart != null) {
            return parseQueryParam(queryPart)
        }

        // Try to get label from query param if available
        val label = queryPart?.let { extractLabel(it) }

        return GeoPoint(coords.first, coords.second, label)
    }

    private fun parseCoords(part: String): Pair<Double, Double>? {
        val clean = part.split(";").first() // Remove any geo URI parameters like ;crs=
        val components = clean.split(",")
        if (components.size < 2) return null
        return try {
            val lat = components[0].trim().toDouble()
            val lng = components[1].trim().toDouble()
            Pair(lat, lng)
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun parseQueryParam(query: String): GeoPoint? {
        val params = query.split("&")
        for (param in params) {
            if (param.startsWith("q=")) {
                val value = param.removePrefix("q=")
                return parseQValue(value)
            }
        }
        return null
    }

    private fun parseQValue(q: String): GeoPoint? {
        // Format: lat,lng(Label) or just lat,lng
        val decoded = try {
            URLDecoder.decode(q, "UTF-8")
        } catch (e: Exception) {
            q
        }

        val labelMatch = Regex("""^(-?[\d.]+),(-?[\d.]+)\((.+)\)$""").find(decoded)
        if (labelMatch != null) {
            return try {
                GeoPoint(
                    latitude = labelMatch.groupValues[1].toDouble(),
                    longitude = labelMatch.groupValues[2].toDouble(),
                    label = labelMatch.groupValues[3]
                )
            } catch (e: NumberFormatException) {
                null
            }
        }

        val coordMatch = Regex("""^(-?[\d.]+),(-?[\d.]+)$""").find(decoded)
        if (coordMatch != null) {
            return try {
                GeoPoint(
                    latitude = coordMatch.groupValues[1].toDouble(),
                    longitude = coordMatch.groupValues[2].toDouble()
                )
            } catch (e: NumberFormatException) {
                null
            }
        }

        return null
    }

    private fun extractLabel(query: String): String? {
        val params = query.split("&")
        for (param in params) {
            if (param.startsWith("q=")) {
                val value = param.removePrefix("q=")
                val decoded = try {
                    URLDecoder.decode(value, "UTF-8")
                } catch (e: Exception) {
                    value
                }
                val match = Regex("""\((.+)\)""").find(decoded)
                return match?.groupValues?.get(1)
            }
        }
        return null
    }
}

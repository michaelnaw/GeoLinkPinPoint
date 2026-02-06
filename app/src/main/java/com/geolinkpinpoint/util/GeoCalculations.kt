package com.geolinkpinpoint.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoCalculations {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val radLat1 = Math.toRadians(lat1)
        val radLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(radLat1) * cos(radLat2) * sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    fun forwardBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val radLat1 = Math.toRadians(lat1)
        val radLat2 = Math.toRadians(lat2)
        val dLng = Math.toRadians(lng2 - lng1)

        val x = sin(dLng) * cos(radLat2)
        val y = cos(radLat1) * sin(radLat2) - sin(radLat1) * cos(radLat2) * cos(dLng)

        val bearing = Math.toDegrees(atan2(x, y))
        return (bearing + 360) % 360
    }

    fun cardinalDirection(degrees: Double): String {
        val normalized = ((degrees % 360) + 360) % 360
        return when {
            normalized < 22.5 || normalized >= 337.5 -> "N"
            normalized < 67.5 -> "NE"
            normalized < 112.5 -> "E"
            normalized < 157.5 -> "SE"
            normalized < 202.5 -> "S"
            normalized < 247.5 -> "SW"
            normalized < 292.5 -> "W"
            else -> "NW"
        }
    }

    fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            String.format("%.2f km", meters / 1000)
        } else {
            String.format("%.1f m", meters)
        }
    }
}

package com.example.gaja_prueba1.model

import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_M = 6_371_000.0

fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return 2 * EARTH_RADIUS_M * asin(sqrt(a))
}

fun Space.distanceFrom(userLat: Double, userLon: Double): Double? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return distanceMeters(userLat, userLon, lat, lon)
}

fun sortByDistance(spaces: List<Space>, userLat: Double, userLon: Double): List<Space> =
    spaces.sortedBy { it.distanceFrom(userLat, userLon) ?: Double.MAX_VALUE }

fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.toInt()} m"
    else String.format(Locale.forLanguageTag("es-CL"), "%.1f km", meters / 1000)

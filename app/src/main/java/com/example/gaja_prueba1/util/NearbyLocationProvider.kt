package com.example.gaja_prueba1.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed interface LocationResult {
    data class Found(val latitude: Double, val longitude: Double) : LocationResult
    data object NoPermission : LocationResult
    data object ProviderDisabled : LocationResult
    data object Unavailable : LocationResult
}

object NearbyLocationProvider {
    private const val TIMEOUT_MS = 10_000L

    fun hasPermission(context: Context): Boolean =
        isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(context: Context): LocationResult {
        val appContext = context.applicationContext
        val fine = isGranted(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = isGranted(appContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!fine && !coarse) return LocationResult.NoPermission

        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(manager)) return LocationResult.ProviderDisabled

        val provider = when {
            fine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return LocationResult.ProviderDisabled
        }

        return try {
            val fresh = withTimeoutOrNull(TIMEOUT_MS) { requestFix(appContext, manager, provider) }
            val location = fresh ?: manager.getLastKnownLocation(provider)
            if (location != null) LocationResult.Found(location.latitude, location.longitude)
            else LocationResult.Unavailable
        } catch (e: SecurityException) {
            LocationResult.NoPermission
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFix(
        context: Context,
        manager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()
        continuation.invokeOnCancellation { signal.cancel() }
        LocationManagerCompat.getCurrentLocation(
            manager,
            provider,
            signal,
            ContextCompat.getMainExecutor(context)
        ) { location ->
            if (continuation.isActive) continuation.resume(location)
        }
    }

    private fun isGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

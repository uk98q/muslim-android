package com.uk98q.muslim

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

/** Uses the last known device location only to compute prayer times. Falls back to Makkah. */
object LocationHelper {
    fun hasPermission(context: Context): Boolean {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    @SuppressLint("MissingPermission")
    fun fetch(context: Context, state: AppState) {
        if (!hasPermission(context)) return
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val best: Location? = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).mapNotNull { provider ->
            try {
                manager.getLastKnownLocation(provider)
            } catch (e: SecurityException) {
                null
            }
        }.maxByOrNull { it.time }
        if (best != null) state.setLocation(best.latitude, best.longitude)
    }
}

package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.example.model.AreaLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationHelper(private val context: Context) {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentGpsLocation(): AreaLocation? = withContext(Dispatchers.IO) {
        try {
            val cts = CancellationTokenSource()
            val location: Location? = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()
            } catch (_: Exception) {
                // Fallback to LocationManager if Google Play Services location fails
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val netLoc = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                gpsLoc ?: netLoc
            }

            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude

                // Reverse Geocode
                var province = "พิกัด GPS"
                var district = "ละติจูด %.4f".format(lat)
                var subDistrict = "ลองจิจูด %.4f".format(lng)

                try {
                    val geocoder = Geocoder(context, Locale("th", "TH"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val addresses = mutableListOf<Address>()
                        val lock = Object()
                        geocoder.getFromLocation(lat, lng, 1) { list ->
                            synchronized(lock) {
                                addresses.addAll(list)
                                lock.notifyAll()
                            }
                        }
                        synchronized(lock) {
                            if (addresses.isEmpty()) {
                                lock.wait(1500)
                            }
                        }
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            province = addr.adminArea ?: province
                            district = addr.subAdminArea ?: addr.locality ?: district
                            subDistrict = addr.subLocality ?: subDistrict
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            province = addr.adminArea ?: province
                            district = addr.subAdminArea ?: addr.locality ?: district
                            subDistrict = addr.subLocality ?: subDistrict
                        }
                    }
                } catch (_: Exception) {
                    // Fallback to closest preset province
                    val closest = findClosestPreset(lat, lng)
                    province = closest.province
                    district = closest.district
                    subDistrict = closest.subDistrict
                }

                AreaLocation(
                    province = province,
                    district = district,
                    subDistrict = subDistrict,
                    latitude = lat,
                    longitude = lng,
                    isGpsAuto = true
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun findClosestPreset(lat: Double, lng: Double): AreaLocation {
        var minDistance = Double.MAX_VALUE
        var closest = AreaLocation.DEFAULT

        for (preset in AreaLocation.PRESET_LOCATIONS) {
            val dLat = preset.latitude - lat
            val dLng = preset.longitude - lng
            val dist = dLat * dLat + dLng * dLng
            if (dist < minDistance) {
                minDistance = dist
                closest = preset
            }
        }
        return closest
    }
}

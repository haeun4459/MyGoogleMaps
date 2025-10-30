package com.example.mygooglemaps

import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MapDirectionHelper {

    private val context = GeoApiContext.Builder()
        .apiKey("AIzaSyCW5MXLusoUzLSFiZGdirbZv7V2h6Zazw0")
        .build()

    suspend fun getRoutePoints(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): List<com.google.android.gms.maps.model.LatLng> = withContext(Dispatchers.IO) {
        try {
            val result = DirectionsApi.newRequest(context)
                .mode(TravelMode.WALKING)
                .language("ko")
                .region("kr")
                .alternatives(true)
                .origin(com.google.maps.model.LatLng(startLat, startLng))
                .destination(com.google.maps.model.LatLng(endLat, endLng))
                .await()

// ✅ 추가: 응답 상태를 콘솔(Logcat)에 출력
            println("✅ Directions API full result: $result")
            println("✅ Routes size: ${result.routes.size}")



            val path = mutableListOf<com.google.android.gms.maps.model.LatLng>()

            if (result.routes.isEmpty()) {
                println("❌ No routes found. Try different coordinates.")
                return@withContext emptyList()
            }

            val route = result.routes[0]
            route.legs.forEach { leg ->
                leg.steps.forEach { step ->
                    step.polyline.decodePath().forEach { latLng ->
                        path.add(
                            com.google.android.gms.maps.model.LatLng(
                                latLng.lat,
                                latLng.lng
                            )
                        )
                    }
                }
            }

            println("✅ Route loaded successfully! Total points: ${path.size}")
            path

        } catch (e: Exception) {
            e.printStackTrace()
            println("🚨 Directions API Error: ${e.message}")
            emptyList()
        }
    }
}

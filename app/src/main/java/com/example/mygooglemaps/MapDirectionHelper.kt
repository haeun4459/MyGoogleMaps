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
                .mode(TravelMode.DRIVING)
                .language("ko")      // ✅ 한국어 응답
                .region("kr")        // ✅ 한국 지역 강제 지정
                .alternatives(true)  // ✅ 대체 경로도 허용
                .origin(com.google.maps.model.LatLng(startLat, startLng))
                .destination(com.google.maps.model.LatLng(endLat, endLng))
                .await()

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

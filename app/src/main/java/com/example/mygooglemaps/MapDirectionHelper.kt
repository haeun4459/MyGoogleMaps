package com.example.mygooglemaps

import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode

object MapDirectionHelper {

    private val context = GeoApiContext.Builder()
        .apiKey("AIzaSyCPQCgAzG4-3D0nzsxU4z7IyFBR3MPyGPc") // 👈 실제 키 입력
        .build()

    fun getRoutePoints(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): List<com.google.android.gms.maps.model.LatLng> {
        val result = DirectionsApi.newRequest(context)
            .mode(TravelMode.DRIVING)
            .origin(com.google.maps.model.LatLng(startLat, startLng))
            .destination(com.google.maps.model.LatLng(endLat, endLng))
            .await()

        val path = mutableListOf<com.google.android.gms.maps.model.LatLng>()

        if (result.routes.isNotEmpty()) {
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
        }
        return path
    }
}

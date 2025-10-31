package com.example.mygooglemaps

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import com.google.android.gms.maps.model.LatLng

object MapDirectionHelper {

    // ✅ Kakao REST API 키 (네 REST API 키로 교체)
    private const val KAKAO_API_KEY = "KakaoAK b0d04327171c2efc6885e9d9a153ca55"

    suspend fun getRoutePoints(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): List<LatLng> = withContext(Dispatchers.IO) {
        val points = mutableListOf<LatLng>()

        try {
            val start = URLEncoder.encode("$startLng,$startLat", "UTF-8")
            val end = URLEncoder.encode("$endLng,$endLat", "UTF-8")

            // ✅ Kakao Mobility Directions API 호출 URL
            val urlStr =
                "https://apis-navi.kakaomobility.com/v1/directions?origin=$start&destination=$end"

            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", KAKAO_API_KEY)

            val response = conn.inputStream.bufferedReader().use { it.readText() }

            val json = JSONObject(response)
            val routes = json.getJSONArray("routes")

            if (routes.length() > 0) {
                val sections = routes.getJSONObject(0).getJSONArray("sections")
                val roads = sections.getJSONObject(0).getJSONArray("roads")

                for (i in 0 until roads.length()) {
                    val road = roads.getJSONObject(i)
                    val vertexes = road.getJSONArray("vertexes")

                    // vertexes 배열은 [x1, y1, x2, y2, x3, y3, ...] 형태임
                    for (j in 0 until vertexes.length() step 2) {
                        val lng = vertexes.getDouble(j)
                        val lat = vertexes.getDouble(j + 1)
                        points.add(LatLng(lat, lng))
                    }
                }
            }

            println("✅ Kakao Directions 경로 좌표 ${points.size}개 불러옴")
        } catch (e: Exception) {
            e.printStackTrace()
            println("🚨 Kakao Directions API 오류: ${e.message}")
        }

        points
    }
}

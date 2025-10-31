package com.example.mygooglemaps

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import com.google.android.gms.maps.model.LatLng

object MapDirectionHelper {

    // ✅ Kakao REST API 키 (REST 키 앞에 KakaoAK 붙여야 함)
    private const val KAKAO_API_KEY = "KakaoAK b0d04327171c2efc6885e9d9a153ca55"

    // ✅ 1. 기존 경로 불러오기 함수 (그대로 유지)
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

    suspend fun getLatLngFromAddress(address: String, apiKey: String): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            try {
                // 1️⃣ 주소를 UTF-8로 인코딩
                val encodedAddress = URLEncoder.encode(address, "UTF-8")

                // 2️⃣ URL 객체 생성 (⚠️ spec = 절대 넣지 말기!)
                val url = URL("https://dapi.kakao.com/v2/local/search/address.json?query=${encodedAddress}")


                // 3️⃣ 로그 확인용 (요청 주소 확인)
                println("📤 요청 주소 확인: $url")

                // 4️⃣ HTTP 연결 생성 및 헤더 설정
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "KakaoAK $apiKey")

                // 5️⃣ 응답 받기
                val response = connection.inputStream.bufferedReader().readText()
                val jsonObject = JSONObject(response)
                val documents = jsonObject.getJSONArray("documents")

                if (documents.length() > 0) {
                    val first = documents.getJSONObject(0)
                    val lat = first.getDouble("y")
                    val lng = first.getDouble("x")
                    println("📍 Kakao API 주소검색 결과: $address -> ($lat, $lng)")
                    return@withContext Pair(lat, lng)
                } else {
                    println("❌ 주소 결과 없음: $address")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

}

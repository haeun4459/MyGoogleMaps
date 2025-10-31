package com.example.mygooglemaps

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

// ✅ Kakao SDK import
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Kakao SDK 초기화 (네이티브 앱 키 넣기)
        KakaoSdk.init(this, "d75453336091add4a1ac5c5a69078515")

        // ✅ 카카오 KeyHash 출력
        val keyHash = Utility.getKeyHash(this)
        Log.e("KAKAO_KEY_HASH", keyHash)

        // ✅ UI (지도 + 경로)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MapWithDirection()
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapWithDirection() {
    var hasLocationPermission by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
            permissionRequested = true
        }
    )

    // ✅ 위치 권한 요청
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    when {
        hasLocationPermission -> {
            // ✅ 출발지/도착지 설정
            val seoul = LatLng(37.5665, 126.9780) // 서울시청
            val gangnam = LatLng(37.4979, 127.0276) // 강남역

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(seoul, 10f)
            }

            var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
            val scope = rememberCoroutineScope()

            // ✅ Directions API 호출
            LaunchedEffect(Unit) {
                scope.launch {
                    try {
                        println("📡 요청 중: ${seoul.latitude},${seoul.longitude} → ${gangnam.latitude},${gangnam.longitude}")

                        val result = MapDirectionHelper.getRoutePoints(
                            seoul.latitude,
                            seoul.longitude,
                            gangnam.latitude,
                            gangnam.longitude
                        )
                        routePoints = result
                        if (result.isEmpty()) {
                            println("❌ No route points returned (check Directions API)")
                        } else {
                            println("✅ Route points loaded: ${result.size}")
                        }
                    } catch (e: Exception) {
                        println("🚨 Directions API Error: ${e.message}")
                    }
                }
            }

            // ✅ 지도 표시
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = seoul),
                    title = "출발지: 서울시청"
                )
                Marker(
                    state = MarkerState(position = gangnam),
                    title = "도착지: 강남역"
                )

                // ✅ 경로선 표시
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = Color(0xFF1E88E5),
                        width = 10f
                    )
                }
            }
        }

        permissionRequested && !hasLocationPermission -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("위치 권한이 필요합니다.")
            }
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

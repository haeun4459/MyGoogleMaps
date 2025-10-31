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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Kakao SDK 초기화 (네이티브 앱 키)
        KakaoSdk.init(this, "d75453336091add4a1ac5c5a69078515")

        // ✅ KeyHash 로그 출력
        val keyHash = Utility.getKeyHash(this)
        Log.e("KAKAO_KEY_HASH", keyHash)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KakaoAddressSearchMap()
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun KakaoAddressSearchMap() {
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
            val cameraPositionState = rememberCameraPositionState()
            val scope = rememberCoroutineScope()
            var searchedLocation by remember { mutableStateOf<LatLng?>(null) }

            // ✅ Kakao 주소검색 (서울역 → 좌표 변환)
            LaunchedEffect(Unit) {
                scope.launch {
                    val kakaoApiKey = "b0d04327171c2efc6885e9d9a153ca55" // ← REST API 키
                    val result = MapDirectionHelper.getLatLngFromAddress("서울특별시 용산구 한강대로 405", kakaoApiKey)


                    result?.let { (lat, lng) ->
                        searchedLocation = LatLng(lat, lng)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(searchedLocation!!, 15f)
                        println("✅ Kakao 주소 검색 성공: 서울역 → $searchedLocation")
                    } ?: println("❌ Kakao 주소 검색 실패")
                }
            }

            // ✅ 지도 표시
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                searchedLocation?.let {
                    Marker(state = MarkerState(position = it), title = "검색 결과: 서울역")
                }
            }
        }

        permissionRequested && !hasLocationPermission -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("위치 권한이 필요합니다.")
            }
        }

        else -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

package com.example.mygooglemaps

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LiveLocationMap(fusedLocationClient)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LiveLocationMap(fusedLocationClient: FusedLocationProviderClient) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
            permissionRequested = true
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    when {
        hasLocationPermission -> {
            val cameraPositionState = rememberCameraPositionState()
            val routePoints = remember { mutableStateListOf<LatLng>() }

            // ✅ 1️⃣ 현재 위치 받아오기
            LaunchedEffect(Unit) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
                    }
                }
            }

            // ✅ 2️⃣ 출발 ~ 도착 경로 받아오기 (비동기)
            LaunchedEffect(Unit) {
                val route = withContext(Dispatchers.IO) {
                    MapDirectionHelper.getRoutePoints(
                        startLat = 37.5665,   // 출발 (서울)
                        startLng = 126.9780,
                        endLat = 37.3943,     // 도착 (성남 근처)
                        endLng = 127.1107
                    )
                }
                routePoints.addAll(route)
            }

            // ✅ 3️⃣ 지도 표시
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                // 파란 경로 선
                if (routePoints.isNotEmpty()) {
                    Polyline(points = routePoints, color = androidx.compose.ui.graphics.Color.Blue)
                }
            }
        }

        permissionRequested && !hasLocationPermission -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("위치 권한이 필요합니다.")
            }
        }

        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

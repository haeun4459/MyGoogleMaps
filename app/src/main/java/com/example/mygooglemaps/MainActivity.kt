package com.example.mygooglemaps   // ← 네 Gradle namespace에 맞게 수정!

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
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

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MapWithMyLocation(fusedLocationClient)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapWithMyLocation(fusedLocationClient: FusedLocationProviderClient) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
            permissionRequested = true
        }
    )

    // 처음 실행 시 권한 요청
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // 권한 허용 시 현재 위치 받아오기
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) currentLocation = location
            }
        }
    }

    when {
        hasLocationPermission && currentLocation != null -> {
            val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(latLng, 16f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = latLng),
                    title = "현재 위치"
                )
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

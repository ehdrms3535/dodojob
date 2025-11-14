package com.example.dodojob.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

@Composable
fun DodoNaverMap(
    modifier: Modifier = Modifier,
    initialCameraPosition: CameraPosition = CameraPosition(
        LatLng(37.5665, 126.9780),
        15.0
    ),
    enableMyLocation: Boolean = false,
    // 🔥 추가: 마커 관련 옵션
    markerPosition: LatLng? = null,
    markerCaption: String? = null,

    onCameraIdle: (LatLng) -> Unit = {},
    onMapReadyExtra: (NaverMap) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }

    val mapView = remember {
        MapView(context).apply { onCreate(Bundle()) }
    }

    DisposableEffect(Unit) {
        mapView.onStart(); mapView.onResume()
        onDispose {
            mapView.onPause(); mapView.onStop(); mapView.onDestroy()
        }
    }

    val locationSource = remember(activity, enableMyLocation) {
        if (enableMyLocation && activity != null) {
            FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE)
        } else null
    }



    LaunchedEffect(mapView, initialCameraPosition, locationSource) {
        mapView.getMapAsync { naverMap ->
            naverMap.uiSettings.apply {
                isCompassEnabled = false
                isZoomControlEnabled = false
                isLocationButtonEnabled = false
            }

            naverMap.cameraPosition = initialCameraPosition

            if (locationSource != null) {
                naverMap.locationSource = locationSource
                naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
            }

            // 🔵 여기서 마커 찍기
            if (markerPosition != null) {
                val marker = com.naver.maps.map.overlay.Marker().apply {
                    position = markerPosition
                    markerCaption?.let { captionText = it }
                    map = naverMap
                }
            }

            onMapReadyExtra(naverMap)

            naverMap.addOnCameraIdleListener {
                onCameraIdle(naverMap.cameraPosition.target)
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

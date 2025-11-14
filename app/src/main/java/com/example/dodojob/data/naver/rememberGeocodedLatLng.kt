package com.example.dodojob.data.naver

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.dodojob.data.naver.NaverGeocoding
import com.naver.maps.geometry.LatLng

/**
 * 주소 문자열을 넣으면
 * - NaverGeocoding.geocode(context, address)를 한 번 호출해서
 * - 결과 LatLng?를 기억해주는 Composable 헬퍼
 */
@Composable
fun rememberGeocodedLatLng(
    address: String
): LatLng? {
    val context = LocalContext.current
    var mapCenter by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(address) {
        val q = address.trim()
        if (q.isEmpty()) {
            mapCenter = null
            return@LaunchedEffect
        }

        try {
            val r = NaverGeocoding.geocode(context, q)
            mapCenter = r?.let { LatLng(it.lat, it.lng) }
        } catch (e: Exception) {
            Log.e("Geocode", "geocode error", e)
            mapCenter = null
        }
    }

    return mapCenter
}
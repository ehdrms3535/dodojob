package com.example.dodojob.ui.feature.prefer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import kotlin.math.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferWorkMapScreen(nav: NavController) {
    var showSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun dismiss() {
        showSheet = false
        nav.popBackStack()
    }

    if (!showSheet) {
        Box(Modifier.fillMaxSize())
        return
    }

    ModalBottomSheet(
        onDismissRequest = { dismiss() },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Box(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(width = 122.dp, height = 4.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFFB3B3B3))
                )
            }
        }
    ) {
        val context = LocalContext.current
        val activity = remember { context.findActivity() }
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }

        LaunchedEffect(activity) {
            if (activity != null) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        var query by remember { mutableStateOf("") }
        var slider by remember { mutableStateOf(0.5f) } // 0 = 가까운 동, 0.5 = 기본, 1 = 먼 동
        var picked by remember { mutableStateOf("태전동") }

        // 🔧 슬라이더 → 반경/민감도 파생값
        val searchRadiusM by remember(slider) {
            // 0 → 600m, 0.5 → 1500m, 1 → 3000m (선형 보간)
            mutableStateOf(600.0 + (slider * 2400.0))
        }
        val moveThresholdM by remember(slider) {
            // 반경의 20% (최소 50m, 최대 300m). 반경이 큰 모드일수록 더 큰 이동이 있어야 호출
            mutableStateOf(max(50.0, min(300.0, searchRadiusM * 0.2)))
        }
        val cachePrecision by remember(slider) {
            // 반경 커질수록 격자(캐시)도 거칠게: 0.001도≈111m
            // 목표 셀 크기 ≈ 반경/6
            val cellM = searchRadiusM / 6.0
            val precision = when {
                cellM >= 111000 -> 0 // ~111km
                cellM >= 11100  -> 1 // ~11.1km
                cellM >= 1110   -> 2 // ~1.11km
                cellM >= 111    -> 3 // ~111m
                else            -> 4 // ~11m
            }
            mutableStateOf(precision)
        }

        // MapView + lifecycle
        val mapView = rememberMapViewWithLifecycle()
        val locationSource = remember(activity) {
            activity?.let { FusedLocationSource(it, LOCATION_PERMISSION_REQUEST_CODE) }
        }

        // 지도 중심 좌표를 비동기적으로 수집해서 디바운스 처리
        var pendingCenter by remember { mutableStateOf<LatLng?>(null) }
        var lastResolved by remember { mutableStateOf<LatLng?>(null) }

        // 디바운스 + 최소 이동 거리 + 캐시 사용
        LaunchedEffect(pendingCenter, moveThresholdM, cachePrecision) {
            val center = pendingCenter ?: return@LaunchedEffect
            delay(380) // 살짝 디바운스 (슬라이더 바뀌어도 자연스럽게)

            val last = lastResolved
            if (last != null) {
                val dist = haversineMeters(last.latitude, last.longitude, center.latitude, center.longitude)
                if (dist < moveThresholdM) return@LaunchedEffect
            }

            picked = "로딩 중..."
            val dong = reverseGeocodeDongCached(
                context = context,
                lat = center.latitude,
                lng = center.longitude,
                precision = cachePrecision
            )
            if (!dong.isNullOrBlank()) {
                picked = dong
                lastResolved = center
            } else {
                picked = "지역 확인 실패"
            }
        }

        // 지도 준비 콜백
        val onMapReady = remember(locationSource) {
            OnMapReadyCallback { naverMap ->
                naverMap.uiSettings.apply {
                    isCompassEnabled = false
                    isZoomControlEnabled = false
                    isLocationButtonEnabled = false
                    val rightPx = with(density) { 16.dp.toPx().toInt() }
                    setLogoMargin(0, 0, rightPx, 0)
                }
                naverMap.cameraPosition = CameraPosition(LatLng(37.5665, 126.9780), 15.0)
                if (locationSource != null) {
                    naverMap.locationSource = locationSource
                    naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
                }

                // 초기 1회: center 전달
                pendingCenter = naverMap.cameraPosition.target

                // 카메라 멈출 때마다 center만 올림 (네트워크 호출은 상위에서)
                naverMap.addOnCameraIdleListener {
                    pendingCenter = naverMap.cameraPosition.target
                }
            }
        }

        LaunchedEffect(mapView) {
            mapView.getMapAsync(onMapReady)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                "일 할 지역을 설정해주세요",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("지역명", color = Color(0xFFA6A6A6), fontSize = 18.sp) },
                trailingIcon = { Icon(Icons.Outlined.Search, null, tint = Color(0xFFA6A6A6)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(57.dp),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEFEFEF),
                    unfocusedContainerColor = Color(0xFFEFEFEF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF005FFF)
                )
            )

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFCFCFCF))
            Spacer(Modifier.height(12.dp))

            // ===== 네이버 지도 =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(10.dp))
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.matchParentSize()
                )

                // 중앙 조준 박스
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                )

                // 좌하단 GPS
                Icon(
                    imageVector = Icons.Outlined.GpsFixed,
                    contentDescription = "GPS",
                    tint = Color(0xFF343330),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .size(32.dp)
                        .clickable {
                            mapView.getMapAsync { map ->
                                if (locationSource != null) {
                                    map.locationTrackingMode = LocationTrackingMode.Follow
                                    val pos: LatLng? = map.locationOverlay?.position
                                    if (pos != null) {
                                        map.moveCamera(CameraUpdate.scrollTo(pos))
                                        pendingCenter = pos // 바로 반영
                                    }
                                }
                            }
                        }
                )

                // 우하단 현재지역 표시
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x2B005FFF))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text("현재지역", color = Color(0xFF005FFF), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(22.dp))

            // 반경 안내 (예: 1.5km 형태로 표시)
            val radiusKm = (searchRadiusM / 1000.0)
            Text(
                "$picked 기준 반경 ${"%.1f".format(radiusKm)} km 탐색",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                color = Color.Black
            )

            Spacer(Modifier.height(12.dp))

            // 🔧 슬라이더: 0(가까운 동) ← → 1(먼 동)
            Slider(
                value = slider,
                onValueChange = { slider = it },
                valueRange = 0f..1f,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF005FFF),
                    activeTrackColor = Color(0xFF005FFF),
                    inactiveTrackColor = Color(0xFFB0C4DE)
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("가까운 동", fontSize = 16.sp)
                Text("기본", fontSize = 16.sp)
                Text("먼 동", fontSize = 16.sp)
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    // 반경/모드와 함께 전달하고 싶으면 savedStateHandle에 같이 저장
                    nav.previousBackStackEntry?.savedStateHandle?.apply {
                        set("pickedRegion", picked)
                        set("searchRadiusM", searchRadiusM)
                    }
                    dismiss()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF))
            ) {
                Text("적용하기", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

/* ---------- MapView Lifecycle ---------- */
@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply { onCreate(Bundle()) }
    }
    DisposableEffect(Unit) {
        mapView.onStart(); mapView.onResume()
        onDispose {
            mapView.onPause(); mapView.onStop(); mapView.onDestroy()
        }
    }
    return mapView
}

/* ---------- Naver Reverse Geocoding ---------- */
// 타임아웃/재시도 살짝 튜닝
private val http by lazy {
    OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .callTimeout(java.time.Duration.ofSeconds(8))
        .connectTimeout(java.time.Duration.ofSeconds(4))
        .readTimeout(java.time.Duration.ofSeconds(6))
        .build()
}

/**
 * 신규 Maps(APIGW) 경로 사용
 * GET https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc
 *   ?request=coordsToaddr
 *   &coords={lng},{lat}
 *   &sourcecrs=epsg:4326
 *   &orders=legalcode,admcode,roadaddr,addr
 *   &output=json
 */
private suspend fun reverseGeocodeDongNaver(
    context: Context,
    lat: Double,
    lng: Double
): String? = withContext(Dispatchers.IO) {
    val keyId = context.getString(com.example.dodojob.R.string.ncp_api_key_id)
    val keySecret = context.getString(com.example.dodojob.R.string.ncp_api_key_secret)

    if (keyId.isBlank() || keySecret.isBlank()) {
        android.util.Log.e("RG", "NCP keys are blank. Check strings.xml & Build Variant.")
        return@withContext null
    }

    val httpUrl = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc"
        .toHttpUrl().newBuilder()
        .addQueryParameter("request", "coordsToaddr")
        .addQueryParameter("coords", "$lng,$lat") // ← 경도,위도 순서 (오타 금지)
        .addQueryParameter("sourcecrs", "epsg:4326")
        .addQueryParameter("orders", "legalcode,admcode,roadaddr,addr")
        .addQueryParameter("output", "json")
        .build()

    val req = Request.Builder()
        .url(httpUrl)
        .addHeader("X-NCP-APIGW-API-KEY-ID", keyId)
        .addHeader("X-NCP-APIGW-API-KEY", keySecret)
        .get()
        .build()

    runCatching {
        http.newCall(req).execute().use { res ->
            val body = res.body?.string()
            if (!res.isSuccessful) {
                android.util.Log.e("RG", "HTTP ${res.code}: $body")
                return@use null
            }
            parseDongFromNaver(body ?: return@use null)
        }
    }.onFailure {
        android.util.Log.e("RG", "Network error", it)
    }.getOrNull()
}

/** 캐시 (격자 라운딩 기반) */
private val dongCache = mutableMapOf<String, String>()

private suspend fun reverseGeocodeDongCached(
    context: Context,
    lat: Double,
    lng: Double,
    precision: Int // 0~6 정도
): String? {
    val key = gridKey(lat, lng, precision)
    dongCache[key]?.let { return it }
    val result = reverseGeocodeDongNaver(context, lat, lng)
    if (!result.isNullOrBlank()) dongCache[key] = result
    return result
}

private fun gridKey(lat: Double, lng: Double, precision: Int): String {
    val f = 10.0.pow(precision)
    val la = round(lat * f) / f
    val ln = round(lng * f) / f
    return "$la,$ln"
}

/** 응답 JSON에서 동/읍/면 추출 */
private fun parseDongFromNaver(json: String): String? {
    val root = JSONObject(json)
    val results = root.optJSONArray("results") ?: return null
    for (i in 0 until results.length()) {
        val region = results.optJSONObject(i)?.optJSONObject("region") ?: continue
        val area3 = region.optJSONObject("area3") // 구·군
        val area4 = region.optJSONObject("area4") // 동/읍/면 or 리
        val a4name = area4?.optString("name").orEmpty()
        val a3name = area3?.optString("name").orEmpty()
        val candidate = when {
            a4name.endsWith("동") || a4name.endsWith("읍") || a4name.endsWith("면") -> a4name
            a3name.endsWith("동") || a3name.endsWith("읍") || a3name.endsWith("면") -> a3name
            else -> a4name.ifBlank { a3name }
        }
        if (candidate.isNotBlank()) return candidate
    }
    return null
}

/* ---------- Context → Activity ---------- */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/* ---------- 거리 계산 ---------- */
private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat/2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon/2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dodojob.ui.feature.prefer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dodojob.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import kotlin.math.*

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

@Composable
fun RegionPickerBottomSheet(
    onApply: (pickedDong: String, searchRadiusM: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(true) }
    if (!showSheet) return

    ModalBottomSheet(
        onDismissRequest = { showSheet = false; onDismiss() },
        sheetState = sheetState,
        containerColor = Color.White,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            Box(
                Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(width = 122.dp, height = 4.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFFB3B3B3))
                )
            }
        }
    ) {
        RegionPickerContent(
            onApply = { picked, radius ->
                showSheet = false
                onApply(picked, radius)
            }
        )
    }
}

/* ====== 바텀시트 내부 내용 ====== */
@Composable
private fun RegionPickerContent(
    onApply: (pickedDong: String, searchRadiusM: Double) -> Unit // ✅ 두 개의 파라미터로 통일
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val activity = remember { context.findActivity() }

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
    var picked by remember { mutableStateOf("태전동") }
    var slider by remember { mutableStateOf(0.5f) } // 0, 0.5, 1

    val mode = when (slider) {
        0f -> "NEAR"
        0.5f -> "MID"
        else -> "FAR"
    }

    val searchRadiusM by remember(mode) {
        mutableStateOf(
            when (mode) {
                "NEAR" -> 800.0      // 0.8 km
                "MID"  -> 1500.0     // 1.5 km
                else   -> 3000.0     // 3.0 km
            }
        )
    }

    val moveThresholdM by remember(mode) {
        mutableStateOf(
            when (mode) {
                "NEAR" -> 80.0
                "MID"  -> 200.0
                else   -> 300.0
            }
        )
    }

    val cachePrecision by remember(mode) {
        mutableStateOf(
            when (mode) {
                "NEAR" -> 4   // ~11m
                "MID"  -> 3   // ~111m
                else   -> 2   // ~1.11km
            }
        )
    }

    // MapView + lifecycle
    val mapView = rememberMapViewWithLifecycle()
    val locationSource = remember(activity) {
        activity?.let { FusedLocationSource(it, LOCATION_PERMISSION_REQUEST_CODE) }
    }

    // 지도 중심 좌표 디바운스
    var pendingCenter by remember { mutableStateOf<LatLng?>(null) }
    var lastResolved by remember { mutableStateOf<LatLng?>(null) }

    // 근처 동네 N
    var nearbyCount by remember { mutableStateOf<Int?>(null) }

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
            pendingCenter = naverMap.cameraPosition.target
            naverMap.addOnCameraIdleListener {
                pendingCenter = naverMap.cameraPosition.target
            }
        }
    }
    LaunchedEffect(mapView) { mapView.getMapAsync(onMapReady) }

    // 디바운스 + 최소 이동거리 + 캐시 + N 갱신
    LaunchedEffect(pendingCenter, moveThresholdM, cachePrecision) {
        val center = pendingCenter ?: return@LaunchedEffect
        delay(380)
        val last = lastResolved
        if (last != null) {
            val dist = haversineMeters(last.latitude, last.longitude, center.latitude, center.longitude)
            if (dist < moveThresholdM) return@LaunchedEffect
        }
        picked = "로딩 중..."
        val dong = reverseGeocodeDongCached(context, center.latitude, center.longitude, cachePrecision)
        if (!dong.isNullOrBlank()) {
            picked = dong
            lastResolved = center
            nearbyCount = null
            nearbyCount = estimateNearbyDongCount(
                context = context,
                centerLat = center.latitude,
                centerLng = center.longitude,
                radiusM = searchRadiusM,
                precision = cachePrecision,
                mode = mode
            )
        } else {
            picked = "지역 확인 실패"
        }
    }

    // 슬라이더/중심 변경에도 N 갱신
    LaunchedEffect(mode, lastResolved, pendingCenter, cachePrecision) {
        val center = lastResolved ?: pendingCenter ?: return@LaunchedEffect
        nearbyCount = null
        nearbyCount = estimateNearbyDongCount(
            context = context,
            centerLat = center.latitude,
            centerLng = center.longitude,
            radiusM = searchRadiusM,
            precision = cachePrecision,
            mode = mode
        )
    }

    val tightLS = (-0.019).em

    // ====== 바텀시트 내용 UI ======
    Column(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp)
            .heightIn(min = 0.dp, max = 882.dp)
    ) {
        Text(
            "일 할 지역을 설정해주세요",
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = tightLS,
            lineHeight = 39.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Spacer(Modifier.height(8.dp))

        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    "지역명",
                    color = Color(0xFFA6A6A6),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = tightLS
                )
            },
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "검색",
                    modifier = Modifier.size(24.dp)
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(57.dp),
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEFEFEF),
                unfocusedContainerColor = Color(0xFFEFEFEF),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF005FFF)
            )
        )

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFFCFCFCF)
        )
        Spacer(Modifier.height(20.dp))

        // 지도 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(381.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE9E9E9))
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.matchParentSize()
            )

            // 좌측 하단 GPS 버튼
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0x11000000), CircleShape)
                    .clickable {
                        mapView.getMapAsync { map ->
                            if (locationSource != null) {
                                map.locationTrackingMode = LocationTrackingMode.Follow
                                val pos: LatLng? = map.locationOverlay?.position
                                if (pos != null) {
                                    map.moveCamera(CameraUpdate.scrollTo(pos))
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gps),
                    contentDescription = "GPS",
                    modifier = Modifier.size(40.dp)
                )
            }

            // 우하단 현재지역 Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(width = 127.dp, height = 49.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x2B005FFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "현재지역",
                    color = Color(0xFF005FFF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = tightLS
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // “~동과 근처 동네 N개”
        Text(
            text = when (val c = nearbyCount) {
                null -> "$picked 주변 동네 계산 중..."
                else -> "${picked}과 근처 동네 ${c}개"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = tightLS,
            color = Color.Black,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        // 스냅 슬라이더
        SnappingDotSlider(
            value = slider,
            onValueChange = { slider = it }
        )

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("가까운 동", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("먼 동",   fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(38.dp))

        Button(
            onClick = { onApply(picked, searchRadiusM) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FFF)),
        ) {
            Text(
                "적용하기",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.navigationBarsPadding())
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
private val http by lazy {
    OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .callTimeout(java.time.Duration.ofSeconds(8))
        .connectTimeout(java.time.Duration.ofSeconds(4))
        .readTimeout(java.time.Duration.ofSeconds(6))
        .build()
}

private suspend fun reverseGeocodeDongNaver(
    context: Context,
    lat: Double,
    lng: Double
): String? = withContext(Dispatchers.IO) {
    val keyId = context.getString(R.string.ncp_api_key_id)
    val keySecret = context.getString(R.string.ncp_api_key_secret)

    if (keyId.isBlank() || keySecret.isBlank()) {
        android.util.Log.e("RG", "NCP keys are blank. Check strings.xml & Build Variant.")
        return@withContext null
    }

    val httpUrl = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc"
        .toHttpUrl().newBuilder()
        .addQueryParameter("request", "coordsToaddr")
        .addQueryParameter("coords", "$lng,$lat") // 경도,위도
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

private suspend fun reverseGeocodeDongCached(
    context: Context,
    lat: Double,
    lng: Double,
    precision: Int
): String? {
    val key = gridKey(lat, lng, precision)
    dongCache[key]?.let { return it }
    val result = reverseGeocodeDongNaver(context, lat, lng)
    if (!result.isNullOrBlank()) dongCache[key] = result
    return result
}

/** 캐시 (격자 라운딩 기반) */
private val dongCache = mutableMapOf<String, String>()

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

/* ---------- 근처 동네 수 대략 추정 ---------- */
private suspend fun estimateNearbyDongCount(
    context: Context,
    centerLat: Double,
    centerLng: Double,
    radiusM: Double,
    precision: Int,
    mode: String
): Int = withContext(Dispatchers.IO) {
    val (rings, samplesPerRing) = when (mode) {
        "NEAR" -> 1 to 8
        "MID"  -> 2 to 12
        else   -> 3 to 16
    }
    val names = linkedSetOf<String>()
    reverseGeocodeDongCached(context, centerLat, centerLng, precision)?.let { if (it.isNotBlank()) names += it }
    for (r in 1..rings) {
        val dist = radiusM * (r / rings.toDouble())
        for (i in 0 until samplesPerRing) {
            val bearing = (360.0 / samplesPerRing) * i
            val (lat, lng) = offsetLatLng(centerLat, centerLng, dist, bearing)
            reverseGeocodeDongCached(context, lat, lng, precision)?.let { if (it.isNotBlank()) names += it }
        }
    }
    names.size
}

private fun offsetLatLng(lat: Double, lng: Double, distanceM: Double, bearingDeg: Double): Pair<Double, Double> {
    val R = 6371000.0
    val br = Math.toRadians(bearingDeg)
    val ang = distanceM / R
    val lat1 = Math.toRadians(lat)
    val lng1 = Math.toRadians(lng)
    val lat2 = asin(sin(lat1) * cos(ang) + cos(lat1) * sin(ang) * cos(br))
    val lng2 = lng1 + atan2(sin(br) * sin(ang) * cos(lat1), cos(ang) - sin(lat1) * sin(lat2))
    return Math.toDegrees(lat2) to Math.toDegrees(lng2)
}

/* ---------- 스냅 슬라이더 ---------- */
@Composable
fun SnappingDotSlider(
    value: Float,                // 0f, 0.5f, 1f 중 하나
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var raw by remember { mutableStateOf(value.coerceIn(0f, 1f)) }
    LaunchedEffect(value) { raw = value.coerceIn(0f, 1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        // 트랙 + 3개 점
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            val cy = size.height / 2f
            val startX = 0f
            val endX = size.width
            val trackH = 4.dp.toPx()

            // 회색 트랙
            drawRoundRect(
                color = Color(0xFFD9D9D9),
                topLeft = Offset(startX, cy - trackH / 2f),
                size = Size(endX - startX, trackH),
                cornerRadius = CornerRadius(trackH / 2f)
            )

            val stops = listOf(0f, 0.5f, 1f)
            val activeIndex = when {
                raw < 0.25f -> 0
                raw < 0.75f -> 1
                else -> 2
            }
            val r = 8.5.dp.toPx()

            stops.forEachIndexed { idx, f ->
                val cx = startX + (endX - startX) * f
                drawCircle(
                    color = if (idx == activeIndex) Color(0xFF005FFF) else Color(0xFFD9D9D9),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }

        // 드래그 영역만 남긴 Slider (트랙/썸/틱 투명)
        Slider(
            value = raw,
            onValueChange = { raw = it.coerceIn(0f, 1f) },
            valueRange = 0f..1f,
            steps = 0,
            onValueChangeFinished = {
                val snap = when {
                    raw < 0.25f -> 0f
                    raw < 0.75f -> 0.5f
                    else -> 1f
                }
                raw = snap
                onValueChange(snap)
            },
            colors = SliderDefaults.colors(
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                thumbColor = Color.Transparent,
                disabledThumbColor = Color.Transparent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.matchParentSize()
        )
    }
}

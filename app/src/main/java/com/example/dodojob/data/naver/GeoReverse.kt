package com.example.dodojob.data.naver

import android.content.Context
import android.util.Log
import com.example.dodojob.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.time.Duration
import kotlin.math.*

// 공용 OkHttpClient
private val http by lazy {
    OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .callTimeout(Duration.ofSeconds(8))
        .connectTimeout(Duration.ofSeconds(4))
        .readTimeout(Duration.ofSeconds(6))
        .build()
}

/**
 * 격자 기반 캐시를 사용하는 역지오코딩 진입 함수
 */
suspend fun reverseGeocodeDongCached(
    context: Context,
    lat: Double,
    lng: Double,
    precision: Int
): String? {
    val key = gridKey(lat, lng, precision)
    dongCache[key]?.let { return it }

    val result = reverseGeocodeDongNaver(context, lat, lng)
    if (!result.isNullOrBlank()) {
        dongCache[key] = result
    }
    return result
}

/**
 * 중심 좌표 주변에 존재하는 "동/읍/면" 이름 개수를 대략 추정
 */
suspend fun estimateNearbyDongCount(
    context: Context,
    centerLat: Double,
    centerLng: Double,
    radiusM: Double,
    precision: Int,
    mode: String
): Int = withContext(Dispatchers.IO) {
    // 샘플링 강도 (기존 로직 그대로)
    val (rings, samplesPerRing) = when (mode) {
        "NEAR" -> 1 to 8
        "MID"  -> 2 to 12
        else   -> 3 to 16
    }

    val names = linkedSetOf<String>()

    // 중심 좌표 자체
    reverseGeocodeDongCached(context, centerLat, centerLng, precision)
        ?.takeIf { it.isNotBlank() }
        ?.let { names += it }

    // 반지름을 ring 수만큼 나눠 동심원 샘플링
    for (r in 1..rings) {
        val dist = radiusM * (r / rings.toDouble())
        for (i in 0 until samplesPerRing) {
            val bearing = (360.0 / samplesPerRing) * i
            val (lat, lng) = offsetLatLng(centerLat, centerLng, dist, bearing)
            reverseGeocodeDongCached(context, lat, lng, precision)
                ?.takeIf { it.isNotBlank() }
                ?.let { names += it }
        }
    }

    names.size
}

/** 동 이름 캐시 (격자 라운딩 기반) */
private val dongCache = mutableMapOf<String, String>()

/**
 * 위도/경도를 10^precision 단위로 라운딩해서 캐시 키 생성
 * precision=4 → 약 11m, 3 → 약 111m, 2 → 약 1.11km
 */
private fun gridKey(lat: Double, lng: Double, precision: Int): String {
    val f = 10.0.pow(precision)
    val la = round(lat * f) / f
    val ln = round(lng * f) / f
    return "$la,$ln"
}

/**
 * 네이버 Reverse Geocoding API 호출해서 동/읍/면 이름 추출
 */
private suspend fun reverseGeocodeDongNaver(
    context: Context,
    lat: Double,
    lng: Double
): String? = withContext(Dispatchers.IO) {
    val keyId = context.getString(R.string.ncp_api_key_id)
    val keySecret = context.getString(R.string.ncp_api_key_secret)

    if (keyId.isBlank() || keySecret.isBlank()) {
        Log.e("RG", "NCP keys are blank. Check strings.xml & Build Variant.")
        return@withContext null
    }

    val httpUrl = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc"
        .toHttpUrl().newBuilder()
        .addQueryParameter("request", "coordsToaddr")
        .addQueryParameter("coords", "$lng,$lat") // 경도,위도 순
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
                Log.e("RG", "HTTP ${res.code}: $body")
                return@use null
            }
            parseDongFromNaver(body ?: return@use null)
        }
    }.onFailure {
        Log.e("RG", "Network error", it)
    }.getOrNull()
}

/**
 * 네이버 Reverse Geocode 응답 JSON에서 동/읍/면 이름 추출
 */
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

/**
 * 두 좌표 사이 거리 (미터)
 */
fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

/**
 * 기준점에서 거리/방위각 기준으로 offset 좌표 계산
 */
private fun offsetLatLng(
    lat: Double,
    lng: Double,
    distanceM: Double,
    bearingDeg: Double
): Pair<Double, Double> {
    val R = 6371000.0
    val br = Math.toRadians(bearingDeg)
    val ang = distanceM / R
    val lat1 = Math.toRadians(lat)
    val lng1 = Math.toRadians(lng)

    val lat2 = asin(
        sin(lat1) * cos(ang) +
                cos(lat1) * sin(ang) * cos(br)
    )
    val lng2 = lng1 + atan2(
        sin(br) * sin(ang) * cos(lat1),
        cos(ang) - sin(lat1) * sin(lat2)
    )

    return Math.toDegrees(lat2) to Math.toDegrees(lng2)
}

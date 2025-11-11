package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

suspend fun CountRecentView(
    id: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Int {
    val TAG = "CountRecentView"

    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    val url = "$supabaseUrl/rest/v1/recent_watch"

    Log.d(TAG, "요청 URL = $url")
    Log.d(TAG, "요청 seniorid = $id")

    val response: HttpResponse = http.get(url) {
        parameter("select", "id")
        parameter("seniorid", "eq.$id")

        header("apikey", token)
        header("Authorization", "Bearer $token")
        header("Prefer", "count=exact")   // ✅ 이게 핵심
    }

    val contentRange = response.headers["Content-Range"]
    Log.d(TAG, "status = ${response.status}")
    Log.d(TAG, "raw body = ${response.bodyAsText()}")
    Log.d(TAG, "Content-Range 헤더 = $contentRange")

    val count = contentRange
        ?.substringAfter("/")   // "0-0/1" -> "1"
        ?.toIntOrNull() ?: 0

    Log.d(TAG, "조회된 recent_view 개수 = $count")
    http.close()
    return count
}

package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

suspend fun fetchAnnounceSeniorCount(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Int {
    return try {
        val res = http.get("$supabaseUrl/rest/v1/announcement_senior") {
            parameter("senior_username", "eq.$username")
            parameter("user_status", "eq.unread")
            parameter("select", "announcement_id")
            header("apikey", token)
            header("Authorization", "Bearer $token")
            header("Prefer", "count=exact")   // 🔥 여기로 변경
        }

        res.headers["Content-Range"]
            ?.substringAfter("/")   // "0-9/10" -> "10"
            ?.toIntOrNull()
            ?: 0
    } catch (e: Exception) {
        Log.e("AnnounceSeniorDao", "❌ unread 개수 조회 오류", e)
        0
    }
}

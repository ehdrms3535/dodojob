package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import com.example.dodojob.BuildConfig.SUPABASE_ANON_KEY
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable


@Serializable
data class Apply(
    val announcement_id: Long,
    val created_at: String,
    val health_conditon:String
)

suspend fun fetchAppliedAnnouncements(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): List<Apply> {
    return try {
        http.get("$supabaseUrl/rest/v1/application") {
            parameter("senior_username", "eq.$username")
            parameter("select", "announcement_id,created_at,health_condition")
            header("apikey", token)
            header("Authorization", "Bearer $token")
        }.body()
    } catch (e: Exception) {
        Log.e("ApplicantInfo", "❌ 지원 리스트 조회 오류", e)
        emptyList()
    }
}

suspend fun fetchAppliedCount(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Int {
    return try {
        val res = http.get("$supabaseUrl/rest/v1/application") {
            parameter("senior_username", "eq.$username")
            parameter("select", "announcement_id")  // 아무 컬럼 하나면 됨
            header("apikey", token)
            header("Authorization", "Bearer $token")
            header("Prefer", "count=exact")         // ← 🔥 이게 정식 스펙
        }

        res.headers["Content-Range"]
            ?.substringAfter("/")    // "0-9/10" → "10"
            ?.toIntOrNull()
            ?: 0
    } catch (e: Exception) {
        Log.e("ApplicantInfo", "❌ 지원 개수 조회 오류", e)
        0
    }
}

package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable private data class EmployerStatsArg(val p_username: String)

@Serializable
data class EmployerStatsRow(
    @SerialName("active_count") val activeCount: Int = 0,
    @SerialName("applicants_count") val applicantsCount: Int = 0,
    @SerialName("completed_count") val completedCount: Int = 0
)

suspend fun fetchEmployerStatsByUsername(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): EmployerStatsRow {
    val url = "$supabaseUrl/rest/v1/rpc/employer_dashboard_stats"
    return try {
        Log.d("SUPA", "📡 RPC start username=$username")

        // ★ 배열로 받고 firstOrNull
        val rows: List<EmployerStatsRow> = http.post(url) {
            contentType(ContentType.Application.Json)
            header("apikey", token)
            header("Authorization", "Bearer $token")
            header("Prefer", "params=single-object")
            // ★ Accept 헤더 제거 => 기본은 배열
            setBody(EmployerStatsArg(username))
        }.body()

        val row = rows.firstOrNull() ?: EmployerStatsRow()
        Log.d("SUPA", "✅ RPC rows=$rows -> use=$row")
        row
    } catch (e: Exception) {
        Log.e("SUPA", "❌ RPC 오류: ${e.message}", e)
        EmployerStatsRow()
    }
}

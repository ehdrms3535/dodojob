package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.request.*
import io.ktor.client.call.body
import kotlinx.serialization.Serializable


@Serializable
data class RecentWatchDaoRow(
    val announceid: Long,
    val created_at: String
    // seniorid는 이미 함수 인자로 있으니까 굳이 안 받아도 됨
)

suspend fun getRecentWatchList(
    seniorid: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): List<RecentWatchDaoRow> {
    val url = "$supabaseUrl/rest/v1/recent_watch"

    val rows: List<RecentWatchDaoRow> = http.get(url) {
        parameter("select", "announceid,created_at")
        parameter("seniorid", "eq.$seniorid")          // ✅ 컬럼 이름 맞추기
        parameter("order", "created_at.desc")          // 최근 본 순으로 정렬

        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()

    return rows
}

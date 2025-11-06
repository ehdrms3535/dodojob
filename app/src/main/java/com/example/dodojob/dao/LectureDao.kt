package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class LectureRow(
    val id: Long,
    val title: String? = null,
    val explain: String? = null,
    val category: String? = null,
    val url: String? = null,
    val thumbnail: String? = null
)

suspend fun fetchLectures(
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY,
    limit: Int? = null
): List<LectureRow> {
    val url = "$supabaseUrl/rest/v1/lecture"
    val rows: List<LectureRow> = http.get(url) {
        parameter("select", "id,title,explain,category,url,thumbnail")
        if (limit != null) parameter("limit", limit)
        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()
    return rows
}

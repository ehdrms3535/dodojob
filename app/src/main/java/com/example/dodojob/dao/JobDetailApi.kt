package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import com.example.dodojob.data.jobdetail.JobDetailDto
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ================== 공고 상세 ==================

@Serializable
private data class JobDetailParams(
    @SerialName("p_announcement_id") val announcementId: Long,
    @SerialName("p_username")        val username: String? = null
)

suspend fun fetchJobDetailDto(
    announcementId: Long,
    username: String? = null,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): JobDetailDto? {

    val url = "${supabaseUrl.trimEnd('/')}/rest/v1/rpc/job_detail"

    val response = http.post(url) {
        contentType(ContentType.Application.Json)
        header("apikey", token)
        header("Authorization", "Bearer $token")
        setBody(JobDetailParams(announcementId, username))
    }

    if (!response.status.isSuccess()) {
        val bodyText = response.bodyAsText()
        Log.e(
            "fetchJobDetailDto",
            "Error ${response.status} from $url\nbody=$bodyText"
        )
        return null
    }

    return response.body<JobDetailDto?>()
}

// ================== 좋아요 토글 ==================

@Serializable
private data class ToggleJobLikeParams(
    @SerialName("p_senior_username") val seniorUsername: String,
    @SerialName("p_announcement_id") val announcementId: Long,
    @SerialName("p_like")            val like: Boolean
)

suspend fun toggleJobLikeDao(
    seniorUsername: String,
    announcementId: Long,
    liked: Boolean,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
) {
    val url = "${supabaseUrl.trimEnd('/')}/rest/v1/rpc/toggle_job_like"

    val response = http.post(url) {
        contentType(ContentType.Application.Json)
        header("apikey", token)
        header("Authorization", "Bearer $token")
        setBody(
            ToggleJobLikeParams(
                seniorUsername = seniorUsername,
                announcementId = announcementId,
                like = liked
            )
        )
    }

    if (!response.status.isSuccess()) {
        val body = response.bodyAsText()
        Log.e("toggleJobLikeDao", "Error ${response.status}: $body")
    }
}



package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import com.example.dodojob.data.jobdetail.JobDetailDto
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// job_detail RPC 호출용 파라미터
@Serializable
private data class JobDetailParams(
    @SerialName("p_announcement_id") val announcementId: Long,
    @SerialName("p_username")        val username: String? = null
)

// 공고 상세 조회
suspend fun fetchJobDetailDto(
    announcementId: Long,
    username: String? = null,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): JobDetailDto? {
    val url = "$supabaseUrl/rest/v1/rpc/job_detail"

    return http.post(url) {
        contentType(ContentType.Application.Json)
        header("apikey", token)
        header("Authorization", "Bearer $token")
        setBody(JobDetailParams(announcementId, username))
    }.body<JobDetailDto?>()
}

@Serializable
private data class ToggleJobLikeParams(
    @SerialName("p_senior_username") val seniorUsername: String,
    @SerialName("p_announcement_id") val announcementId: Long,
    @SerialName("p_company_id")      val companyId: String? = null,
    @SerialName("p_like")            val like: Boolean
)

/*
 - 좋아요 토글용 RPC 호출
 - 함수명: toggle_job_like
 - 파라미터: p_senior_username, p_announcement_id, p_company_id, p_like
 */
suspend fun toggleJobLikeDao(
    seniorUsername: String,
    announcementId: Long,
    companyId: String?,
    liked: Boolean,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
) {
    val url = "$supabaseUrl/rest/v1/rpc/toggle_job_like"

    http.post(url) {
        contentType(ContentType.Application.Json)
        header("apikey", token)
        header("Authorization", "Bearer $token")
        setBody(
            ToggleJobLikeParams(
                seniorUsername = seniorUsername,
                announcementId = announcementId,
                companyId = companyId,
                like = liked
            )
        )
    }
}



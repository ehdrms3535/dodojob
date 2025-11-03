// com.example.dodojob.dao.job_detail_rpc.kt
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
    val url = "$supabaseUrl/rest/v1/rpc/job_detail"
    return http.post(url) {
        contentType(ContentType.Application.Json)
        header("apikey", token)
        header("Authorization", "Bearer $token")
        setBody(JobDetailParams(announcementId, username))
    }.body<JobDetailDto?>()
}

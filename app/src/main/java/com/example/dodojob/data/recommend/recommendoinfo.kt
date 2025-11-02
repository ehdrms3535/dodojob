package com.example.dodojob.data.recommend

import kotlinx.serialization.Serializable
import io.ktor.client.*                     // HttpClient
import io.ktor.client.engine.okhttp.*       // OkHttp 엔진
import io.ktor.client.plugins.contentnegotiation.* // ContentNegotiation
import io.ktor.client.request.*             // get, post, header, setBody
import io.ktor.client.statement.*           // HttpResponse
import io.ktor.http.*                       // ContentType, HttpHeaders
import io.ktor.serialization.kotlinx.json.* // json(Json)
import kotlinx.serialization.json.Json      // Json 설정

import com.example.dodojob.BuildConfig      // BuildConfig.SUPABASE_ANON_KEY 등
import io.ktor.client.call.body
import com.example.dodojob.data.supabase.LocalSupabase
import io.github.jan.supabase.postgrest.rpc

@Serializable
data class RecoJob(
    val id: Long,
    val company_name: String? = null,
    val company_locate: String? = null,
    val job_category: String? = null,
    val talent: String? = null,
    val major: String? = null,
    val form: String? = null,
    val week_text: String? = null,
    val starttime: String? = null,
    val endtime: String? = null,
    val intensity: String? = null,
    val salary_type: String? = null,
    val salary_amount: Long? = null,
    val benefit: String? = null, // ← 응답에 없으면 지우는 걸 권장. 남길거면 = null 꼭!
    val career: String? = null,
    val job_gender: String? = null,
    val is_public: Boolean? = null,
    val created_at: String? = null,
    val is_paid: Boolean? = null,
    val paid_days: Int? = null,
    val career_required: Boolean? = null,
    val similarity: Double? = null
)

suspend fun fetchRecommendedJobs(
    region: String? = null,
    category: String? = null,
    days: List<String>? = null,
    startMin: Int? = null,
    endMin: Int? = null,
    years: Int = 0,
    gender: String? = null
): List<RecoJob> {
    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    try {
        val res: List<RecoJob> = http.post(
            "https://bswcjushfcwsxswufejm.supabase.co/rest/v1/rpc/reco_candidates"
        ) {
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "p_region" to region,
                    "p_category" to category,
                    "p_days" to days,          // text[]는 List<String>로
                    "p_start_min" to startMin,
                    "p_end_min" to endMin,
                    "p_years" to years,
                    "p_gender" to gender
                )
            )
        }.body()
        return res
    } finally {
        http.close() // 임시 생성이면 닫아주기(앱 전체에서 재사용한다면 싱글톤 추천)
    }
}

suspend fun fetchAiRecommendedJobs(username: String): List<RecoJob> {
    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    try {
        val res: List<RecoJob> = http.post(
            "https://bswcjushfcwsxswufejm.supabase.co/rest/v1/rpc/reco_candidates_ai_v2"
        ) {
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "p_username" to username
                )
            )
        }.body()
        return res
    } finally {
        http.close() // 임시 생성이면 닫아주기(앱 전체에서 재사용한다면 싱글톤 추천)
    }
}
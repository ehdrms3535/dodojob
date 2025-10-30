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

@Serializable
data class RecoJob(
    val id: Long,
    val company_name: String?,
    val company_locate: String?,
    val job_category: String?,
    val talent: String?,
    val major: String?,
    val form: String?,
    val week_text: String?,
    val starttime: String?,
    val endtime: String?,
    val intensity: String?,
    val salary_type: String?,
    val salary_amount: Long?,
    val benefit: String?,
    val career: String?,
    val job_gender: String?,
    val is_public: Boolean?,
    val created_at: String?
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

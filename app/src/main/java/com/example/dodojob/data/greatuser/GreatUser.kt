package com.example.dodojob.data.greatuser

import com.example.dodojob.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GreatUser(
    val name: String? = null,
    val gender : String? = null,
    // Supabase date → 문자열로 먼저 받는 게 가장 안전
    val birthdate : String? = null,
    val region : String? = null,
    val phone : String? = null,
    val email : String? = null,
    val username : String? = null,
    val password : String? = null,
    val job : String? = null,

    val jobtype : String? = null,
    val locate : String? = null,
    val job_talent : String? = null,
    val job_manage : String? = null,
    val job_service : String? = null,
    val job_care : String? = null,
    val term : String? = null,
    val days : Boolean? = null,
    val weekend : Boolean? = null,
    val week : String? = null,
    val time : Boolean? = null,

    val license_name : String? = null,
    val license_location : String? = null,
    val license_number : String? = null,

    val company : String? = null,
    val career_title : String? = null,
    val start_date : String? = null,
    val end_date : String? = null,
    val description : String? = null,

    val applyCount : Long? = null,
    val resumeViews : Long? = null,
    val recentCount : Long? = null,
    val likedCount : Long? = null,
    val activityLevel : Long? = null,     // 뷰에서 camelCase로 나가면 그대로, snake면 activity_level로 필드명을 맞추세요
    val applyWithinYear : Long? = null,
    val realWorkExpCount : Long? = null,
    val eduCompleted : Boolean? = null
)

private val http by lazy {
    HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
    }
}

suspend fun fetchGreatUser(): List<GreatUser> {
    val url = "https://bswcjushfcwsxswufejm.supabase.co/rest/v1/great_user_view"

    // ⚠️ DB 컬럼명이 snake_case 라는 가정: activity_level
    //    만약 뷰 컬럼명이 camelCase(activityLevel)라면 아래 parameter 키도 activityLevel 로 바꾸세요.
    return http.get(url) {
        // 꼭 select 지정
        parameter("select", "*")
        parameter("activityLevel", "gte.3")  // ← 컬럼명 맞추기 (snake_case)

        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        header("Accept", ContentType.Application.Json)
        // 페이지 일부만 원하면:
        // header("Range", "0-49")
        // header("Prefer", "count=exact")
    }.body()
}

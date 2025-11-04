package com.example.dodojob.data.announcement.fullannouncement

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.example.dodojob.BuildConfig

@Serializable
data class AnnouncementFullRow(
    val announcement_id: Long,
    val company_name: String? = null,
    val company_id: String? = null,
    val company_locate: String? = null,
    val detail_locate: String? = null,
    val created_at: String? = null,
    val public: Boolean? = null,

    val salary_type: String? = null,   // "시급"/"월급"/...
    val salary_amount: Int? = null,    // 금액
    val benefit: String? = null,       // 예: "100110" (비트 문자열)

    val career: String? = null,
    val gender: String? = null,

    val work_category: String? = null,
    val talent: String? = null,        // 10자리 비트문자열 등
    val major: String? = null,         // 업무 내용
    val form: String? = null,          // 근무 형태
    val week: String? = null,          // 예: "1111111"
    val starttime: String? = null,     // "오전 1:00"
    val endtime: String? = null,       // "오후 12:00"
    val intensity: String? = null,

    val preferential_treatment: String? = null, // 집 가까움, 장기근무 우대, ...
    val skill: String? = null,                  // 경력/스킬 문자열
    val license_requirement: String? = null,    // 자격요건

    val is_paid: Boolean? = null,
    val paid_days: Long? = null,

    val company_imgurl: String? = null,
    val company_imgurl2: String? = null,
    val company_imgurl3: String? = null,
    val company_imgurl4: String? = null
)

suspend fun fetchAnnouncementFull(
    id: Long?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY // or 세션 토큰
): AnnouncementFullRow? {

    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val url = "$supabaseUrl/rest/v1/announcement_full_view"
    val rows: List<AnnouncementFullRow> = http.get(url) {
        parameter("announcement_id", "eq.$id")
        parameter("select", "*")
        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()
    return rows.firstOrNull()
}

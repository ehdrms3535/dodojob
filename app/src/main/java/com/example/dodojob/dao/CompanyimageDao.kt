// com.example.dodojob.dao.CompanyImages.kt
package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.call.body

@Serializable
data class CompanyImagesRow(
    val id: Long,                 // = announcement.id
    val company_imgurl: String? = null,
    val company_imgurl2: String? = null,
    val company_imgurl3: String? = null,
    val company_imgurl4: String? = null
)

// 공고 id 리스트를 받아  id -> 대표 이미지 URL  매핑을 돌려줌
suspend fun fetchCompanyImagesMap(
    announcementIds: List<Long>,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Map<Long, String> {
    if (announcementIds.isEmpty()) return emptyMap()

    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    return try {
        // PostgREST: /company_images?id=in.(1,2,3)
        val url = "$supabaseUrl/rest/v1/company_images"
        val idsCsv = announcementIds.joinToString(",")

        val rows: List<CompanyImagesRow> = http.get(url) {
            url {
                parameters.append("id", "in.($idsCsv)")
                parameters.append(
                    "select",
                    "id,company_imgurl,company_imgurl2,company_imgurl3,company_imgurl4"
                )
            }
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            header("Authorization", "Bearer $token")
        }.body()

        // 각 행에서 첫 번째로 존재하는 이미지 하나를 대표로 선택
        rows.associate { r ->
            val first = listOf(
                r.company_imgurl, r.company_imgurl2, r.company_imgurl3, r.company_imgurl4
            ).firstOrNull { !it.isNullOrBlank() }
            r.id to (first ?: "")
        }.filterValues { it.isNotBlank() }
    } finally {
        http.close()
    }
}

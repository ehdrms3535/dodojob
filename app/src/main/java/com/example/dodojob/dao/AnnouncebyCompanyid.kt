package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.request.*
import io.ktor.client.call.body
import kotlinx.serialization.Serializable


@Serializable
data class announce(
    val id: Long,
)

suspend fun getannouncebycom(
    companyid: String?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): announce? {
    val url = "$supabaseUrl/rest/v1/announcement"

    val rows: List<announce> = http.get(url) {
        parameter("select","id")
        parameter("company_id", "eq.$companyid")
        parameter("limit", "1")

        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()

    return rows.firstOrNull()
}
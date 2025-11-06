package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class UserNameRow(
    val name: String? = null
)

suspend fun fetchDisplayNameByUsername(
    usernameId: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): String? {
    val url = "$supabaseUrl/rest/v1/users_tmp"
    val rows: List<UserNameRow> = http.get(url) {
        parameter("select", "name")
        parameter("username", "eq.$usernameId")
        parameter("limit", "1")
        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()
    return rows.firstOrNull()?.name
}

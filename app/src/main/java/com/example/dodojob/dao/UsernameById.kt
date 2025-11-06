package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class UsernameRow(
    val name: String? = null
)

suspend fun getUsernameById(
    username: String?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): String? {
    if (username.isNullOrBlank()) return null

    val url = "$supabaseUrl/rest/v1/users_tmp"

    val rows = http.get(url) {
        parameter("select", "name")
        parameter("username", "eq.$username")
        parameter("limit", "1")
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer $token")
        header("Accept", "application/json")
    }.body<List<UsernameRow>>()

    return rows.firstOrNull()?.name
}

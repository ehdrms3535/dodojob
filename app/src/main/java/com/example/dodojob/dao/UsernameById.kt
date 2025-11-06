package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import io.ktor.client.call.body

@Serializable
data class UsernameRow(
    val name: String? = null
)


suspend fun getUsernameById(
    username: String?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY // or 로그인된 세션 토큰
): String? {
    val url = "$supabaseUrl/rest/v1/users_tmp"
    val rows: List<UsernameRow> = http.get(url) {
        url {
            parameters.append("username", "eq.$username")   // where id = userId
            parameters.append("select", "name") // username만 선택
            parameters.append("limit", "1")         // 딱 1건만
        }
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer $token")
    }.body<List<UsernameRow>>()

    return rows.firstOrNull()?.name

}
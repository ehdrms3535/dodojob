package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.request.*
import io.ktor.client.call.body
import kotlinx.serialization.Serializable


@Serializable
data class preuserRow(
    val name: String,
    val phone: String,
    val email: String
)

suspend fun getPreuserInformation(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): preuserRow? {
    val url = "$supabaseUrl/rest/v1/users_tmp"

    val rows: List<preuserRow> = http.get(url) {
        parameter("select","name,phone,email")
        parameter("username", "eq.$username")
        parameter("limit", "1")

        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()

    return rows.firstOrNull()
}




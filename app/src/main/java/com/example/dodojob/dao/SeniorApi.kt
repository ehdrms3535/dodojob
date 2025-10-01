package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import com.example.dodojob.data.senior.SeniorJoined
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter


suspend fun getSeniorInformation(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): SeniorJoined? {
    val url = "$supabaseUrl/rest/v1/senior"

    val rows: List<SeniorJoined> = http.get(url) {
        parameter(
            "select",
            "username,applyCount,resumeViews,recentCount,likedCount,activityLevel," +
                    "users_tmp(username,name,gender,phone,email,password)"
        )
        parameter("username", "eq.$username")
        parameter("limit", "1")

        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()

    return rows.firstOrNull()
}

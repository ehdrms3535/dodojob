package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import com.example.dodojob.data.senior.SeniorJoined
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText   // ✅ 추가

suspend fun getSeniorInformation(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): SeniorJoined? {
    val url = "$supabaseUrl/rest/v1/senior"

    // 1) 먼저 raw response 받기
    val response = http.get(url) {
        parameter(
            "select",
            "username,applyCount,resumeViews,recentCount,likedCount,activityLevel,applyWithinYear,realWorkExpCount,eduCompleted," +
                    "users_tmp(username,name,gender,phone,email,password,created_at,user_image(img_url))"
        )
        parameter("username", "eq.$username")
        parameter("limit", "1")

        header("apikey", token)
        header("Authorization", "Bearer $token")
    }

    // 2) 서버가 뭐라고 응답했는지 그대로 찍기
    val raw = response.bodyAsText()
    println("🔥 RAW RESPONSE getSeniorInformation = $raw")

    // 3) 그 다음에 파싱 시도
    val rows: List<SeniorJoined> = response.body()

    return rows.firstOrNull()
}

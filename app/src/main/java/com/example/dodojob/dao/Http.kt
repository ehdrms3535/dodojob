package com.example.dodojob.dao


import com.example.dodojob.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.*      // ← header(), accept(), contentType() 등

val http = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                encodeDefaults = false
            }
        )
    }
    install(DefaultRequest) {                    // 👈 플러그인 설치
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header(HttpHeaders.Authorization, "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        accept(ContentType.Application.Json)
    }
}

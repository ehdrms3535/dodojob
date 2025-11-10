package com.example.dodojob.data.greatuser

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
data class SCUdto(
    val senior: String
)

private val http by lazy {
    HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
    }
}

suspend fun existSGU(companyId: String, seniorId: String): Boolean {
    val url = "https://bswcjushfcwsxswufejm.supabase.co/rest/v1/scrappedgreatuser"

    val response = http.get(url) {
        parameter("select", "senior")
        parameter("employ", "eq.$companyId")
        parameter("senior", "eq.$seniorId")
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        header("Accept", "application/json")
    }

    val list = response.body<List<SCUdto>>()
    return list.isNotEmpty()
}

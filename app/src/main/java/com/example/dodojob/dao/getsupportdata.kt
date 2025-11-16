package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable   // ✅ 추가



@Serializable
data class SupportData(
    val announcement_id: Long?,
    val senior_username: String?,
    val user_status : String,
    val applied_at : String,
    val company_name: String?,
    val company_locate: String?,
    val major: String?
)


suspend fun fetchSupportDataMerged(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): List<SupportData> {

    return try {
        Log.d("SupportDAO", "▶ fetchSupportDataMerged username=$username")

        val applies: List<SupportData> = http.get("$supabaseUrl/rest/v1/v_senior_support") {
            parameter("senior_username", "eq.$username")
            parameter("select", "announcement_id,senior_username,user_status,applied_at,company_name,company_locate,major")
            header("apikey", token)
            header("Authorization", "Bearer $token")
        }.body()

        Log.d("SupportDAO", "✅ fetchSupportDataMerged size=${applies.size}")
        Log.d("SupportDAO", "✅ first item = ${applies.firstOrNull()}")

        applies
    } catch (e: Exception) {
        Log.e("SupportDAO", "❌ fetchSupportDataMerged error", e)
        throw e
    }
}

@Serializable
data class InterviewsItem(
    val announcement_id: Long,
    val interview_date: String,
    val company_name: String,
    val major: String,
    val address: String,
)

suspend fun fetchInterDataMerged(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): List<InterviewsItem> {

    return try {
        val applies: List<InterviewsItem> = http.get("$supabaseUrl/rest/v1/v_suggest_interview_full") {
            parameter("senior_username", "eq.$username")
            parameter("select", "announcement_id,interview_date,company_name,major,address")
            header("apikey", token)
            header("Authorization", "Bearer $token")
        }.body()

        Log.d("SupportDAO", "✅ fetchSupportDataMerged size=${applies.size}")
        Log.d("SupportDAO", "✅ first item = ${applies.firstOrNull()}")

        applies
    } catch (e: Exception) {
        Log.e("SupportDAO", "❌ fetchSupportDataMerged error", e)
        throw e
    }
}




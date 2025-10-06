package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import com.example.dodojob.data.welfare.WelfareRow
import com.example.dodojob.data.welfare.WelfareSeniorRow
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/** welfare 목록 조회 */
suspend fun getWelfareList(
    category: Boolean? = null,
    query: String? = null,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): List<WelfareRow> {
    val url = "$supabaseUrl/rest/v1/welfare"

    val rows: List<WelfareRow> = http.get(url) {
        parameter("select", "id,welfare_name,welfare_locate,type,explain,offer")

        category?.let { parameter("type", "eq.$it") }
        query?.takeIf { it.isNotBlank() }?.let {
            parameter(
                "or",
                "welfare_name.ilike.%$it%,welfare_locate.ilike.%$it%,explain.ilike.%$it%,offer.ilike.%$it%"
            )
        }

        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()

    return rows
}

/** 내 신청 목록 (welfare_senior + welfare 조인) */
suspend fun getMyWelfareApplications(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): List<WelfareSeniorRow> {
    val url = "$supabaseUrl/rest/v1/welfare_senior"

    val rows: List<WelfareSeniorRow> = http.get(url) {
        parameter(
            "select",
            "id,username,created_at,welfare_status,welfare_id," +
                    "welfare(id,welfare_name,welfare_locate,type,explain,offer)"
        )
        parameter("username", "eq.$username")
        // 필요 시 정렬
        parameter("order", "created_at.desc")

        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()

    return rows
}

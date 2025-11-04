package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import com.example.dodojob.session.CurrentUser
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.statement.*
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * 데이터 모델 정의
 */
@Serializable
data class UserEmailRow(
    val email: String? = null
)

@Serializable
data class CompanyIdRow(
    val companyid: String? = null
)

/**
 * username으로 company_id 조회 (REST 방식)
 */
suspend fun getCompanyIdByUsername(
    username: String?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY // or 세션 토큰
): String? {
    // 1️⃣ HTTP client
    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // 2️⃣ users_tmp에서 email 가져오기
    val userUrl = "$supabaseUrl/rest/v1/users_tmp"
    val userRows: List<UserEmailRow> = http.get(userUrl) {
        url {
            parameters.append("username", "eq.$username")
            parameters.append("select", "email")
            parameters.append("limit", "1")
        }
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer $token")
    }.body()

    val email = userRows.firstOrNull()?.email ?: return null
    Log.d("Announcement2", "useremail=${email}")
    // 3️⃣ employ_tmp에서 company_id 가져오기
    val employUrl = "$supabaseUrl/rest/v1/employ_tmp"
    val employRows: List<CompanyIdRow> = http.get(employUrl) {
        url {
            parameters.append("id", "eq.$email")
            parameters.append("select", "companyid")
            parameters.append("limit", "1")
        }
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer $token")
    }.body()
    val aa = employRows.firstOrNull()?.companyid
    Log.d("Announcement2", "useremail=${aa}")

    // 4️⃣ 결과 반환
    return employRows.firstOrNull()?.companyid
}

suspend fun getCompanyRowCount(
    companyId: String?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Int {
    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // ✅ 여기서 HttpResponse 타입으로 받음
    val response: HttpResponse = http.get("$supabaseUrl/rest/v1/announcement") {
        url {
            parameters.append("company_id", "eq.$companyId")
            parameters.append("select", "company_id")
        }
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer $token")
        header("Prefer", "count=exact") // 행 개수 헤더 받기
    }

    // ✅ response.headers 접근 가능
    val countHeader = response.headers["Content-Range"]
    val count = countHeader?.substringAfter("/")?.toIntOrNull() ?: 0

    return count
}

suspend fun getannounce24(
    companyId: String?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Int{
    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    val now = Instant.now()
    val since24h = now.minus(24, ChronoUnit.HOURS).toString() // → ISO8601 e.g. "2025-10-28T07:00:00Z"

    // ✅ 여기서 HttpResponse 타입으로 받음
    val response: HttpResponse = http.get("$supabaseUrl/rest/v1/announcement_senior") {
        url {
            parameters.append("company_id", "eq.$companyId")
            parameters.append("user_status", "eq.unread")
            parameters.append("created_at", "gte.$since24h")
            parameters.append("select", "company_id")
        }
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer $token")
        header("Prefer", "count=exact") // 행 개수 헤더 받기
    }

    // ✅ response.headers 접근 가능
    val countHeader = response.headers["Content-Range"]
    val count = countHeader?.substringAfter("/")?.toIntOrNull() ?: 0

    return count

}

suspend fun getannounce(
    companyId: String?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Int{
    val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    val now = Instant.now()
    val since24h = now.minus(24, ChronoUnit.HOURS).toString() // → ISO8601 e.g. "2025-10-28T07:00:00Z"

    // ✅ 여기서 HttpResponse 타입으로 받음
    val response: HttpResponse = http.get("$supabaseUrl/rest/v1/announcement_senior") {
        url {
            parameters.append("company_id", "eq.$companyId")
            parameters.append("user_status", "eq.unread")
            parameters.append("select", "company_id")
        }
        header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer $token")
        header("Prefer", "count=exact") // 행 개수 헤더 받기
    }

    // ✅ response.headers 접근 가능
    val countHeader = response.headers["Content-Range"]
    val count = countHeader?.substringAfter("/")?.toIntOrNull() ?: 0

    return count

}
package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import com.example.dodojob.R
import com.example.dodojob.data.career.CareerRepositoryImpl
import com.example.dodojob.ui.feature.main.ApplicantUi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private const val TAG = "RecentApplicants"

@Serializable
data class ApplicantRow(
    val username: String,
    val name: String,
    val region: String,
    val activityLevel: Int,
    val birthdate: String
)

@Serializable
data class AnnounceIdRow(
    val id: Long
)

@Serializable
data class SeniorApplyRow(
    val senior_username: String,
    val created_at: String
)

// 1. employer username -> announcement.id
// 2. announcement.id -> senior_username
// 3. senior_username -> great_user_view + career_senior 합쳐서 ApplicantUi
suspend fun getRecentApplicantsByCompany(
    username: String?,
    careerRepo: CareerRepositoryImpl,
    http: HttpClient,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): List<ApplicantUi> = withContext(Dispatchers.IO) {

    Log.d(TAG, "===== getRecentApplicantsByCompany 시작 =====")
    Log.d(TAG, "입력 username = $username")

    if (username == null) {
        Log.w(TAG, "username 이 null 이라 지원자 목록을 가져오지 않고 빈 리스트 반환")
        return@withContext emptyList()
    }

    // ---------------- 1) employer username -> announcement.id ----------------
    val announceRows: List<AnnounceIdRow> = try {
        Log.d(TAG, "[1단계] announcement 조회 시작 username=$username")

        http.get("$supabaseUrl/rest/v1/announcement") {
            parameter("username", "eq.$username")
            parameter("select", "id")
            header("apikey", token)
            header("Authorization", "Bearer $token")
        }.body()
    } catch (e: Exception) {
        Log.e(TAG, "[1단계] announcement 조회 중 오류 발생", e)
        return@withContext emptyList()
    }

    Log.d(TAG, "[1단계] announcement 개수 = ${announceRows.size}")
    Log.d(TAG, "[1단계] announcement rows = $announceRows")

    val allAnnounceIds = announceRows.map { it.id }.distinct()
    Log.d(TAG, "[1단계] distinct announcement id 목록 = $allAnnounceIds")

    if (allAnnounceIds.isEmpty()) {
        Log.w(TAG, "[1단계] employer 가 가진 공고(id)가 없어 빈 리스트 반환")
        return@withContext emptyList()
    }

    val announceCsv = allAnnounceIds.joinToString(",") { it.toString() }
    Log.d(TAG, "[1단계] announceCsv = $announceCsv")

    // ---------------- 2) announcement.id -> senior_username, created_at ----------------
    val seniorApplyRows: List<SeniorApplyRow> = try {
        Log.d(TAG, "[2단계] announcement_senior 조회 시작, announcement_id in.($announceCsv)")

        http.get("$supabaseUrl/rest/v1/announcement_senior") {
            parameter("announcement_id", "in.($announceCsv)")
            parameter("select", "senior_username,created_at")
            header("apikey", token)
            header("Authorization", "Bearer $token")
        }.body()
    } catch (e: Exception) {
        Log.e(TAG, "[2단계] announcement_senior 조회 중 오류 발생", e)
        return@withContext emptyList()
    }

    Log.d(TAG, "[2단계] announcement_senior 개수 = ${seniorApplyRows.size}")
    // 너무 길면 일부만 보고 싶으면 take(10) 으로 조절
    Log.d(TAG, "[2단계] announcement_senior rows (최대 10개) = ${seniorApplyRows.take(10)}")

    val allSeniorUsernames = seniorApplyRows.map { it.senior_username }.distinct()
    Log.d(TAG, "[2단계] distinct senior_username 목록 = $allSeniorUsernames")

    if (allSeniorUsernames.isEmpty()) {
        Log.w(TAG, "[2단계] 지원한 시니어가 없어 빈 리스트 반환")
        return@withContext emptyList()
    }

    // Supabase in filter 에서 텍스트일 경우, 필요하면 \"username\" 형식으로 감싸는 것도 고려
    val seniorCsv = allSeniorUsernames.joinToString(",") { "\"$it\"" }
    Log.d(TAG, "[2단계] seniorCsv = $seniorCsv")

    // username -> created_at (가장 최근 지원 시각 기준으로 쓰고 싶으면 여기서 정렬/필터 가능)
    val applyTimeMap: Map<String, OffsetDateTime> =
        seniorApplyRows.associate { row ->
            val createdAt = try {
                val parsed = OffsetDateTime.parse(row.created_at)
                Log.d(TAG, "[2단계] created_at 파싱 성공 username=${row.senior_username}, raw=${row.created_at}, parsed=$parsed")
                parsed
            } catch (e: Exception) {
                Log.e(TAG, "[2단계] created_at 파싱 실패 username=${row.senior_username}, raw=${row.created_at}", e)
                // 실패 시 현재 시간으로 대체 (문제 파악 위해 일부러 로그 남김)
                OffsetDateTime.now()
            }
            row.senior_username to createdAt
        }

    Log.d(TAG, "[2단계] applyTimeMap 내용 = $applyTimeMap")

    // ---------------- 3) great_user_view 에서 시니어 요약 정보 ----------------
    val applicantRows: List<ApplicantRow> = try {
        Log.d(TAG, "[3단계] great_user_view 조회 시작, senior_username in.($seniorCsv)")

        http.get("$supabaseUrl/rest/v1/great_user_view") {
            parameter("username", "in.($seniorCsv)")
            parameter(
                "select",
                "username,name,region,activityLevel,birthdate"
            )
            header("apikey", token)
            header("Authorization", "Bearer $token")
        }.body()
    } catch (e: Exception) {
        Log.e(TAG, "[3단계] great_user_view 조회 중 오류 발생", e)
        return@withContext emptyList()
    }

    Log.d(TAG, "[3단계] great_user_view rows 개수 = ${applicantRows.size}")
    Log.d(TAG, "[3단계] great_user_view rows (최대 10개) = ${applicantRows.take(10)}")

    val now = OffsetDateTime.now()
    val nowYear = LocalDate.now().year
    Log.d(TAG, "[공통] now=$now, nowYear=$nowYear")

    val result = applicantRows.map { row ->
        // 3-1) 총 경력(년/개월) 계산
        val (years, months) = careerRepo.totalCareerPeriod(row.username)
        val experienceText = if (years <= 0) {
            "신입"
        } else {
            "경력 ${years}년"
        }

        Log.d(
            TAG,
            "[매핑] username=${row.username} 경력 years=$years, months=$months, text=$experienceText"
        )

        // 3-2) 나이 계산 (birthdate 앞 4자리 = 연도라고 가정)
        val birthYear = row.birthdate.take(4).toIntOrNull() ?: nowYear
        val age = (nowYear - birthYear).coerceAtLeast(0)
        Log.d(
            TAG,
            "[매핑] username=${row.username} birthdate=${row.birthdate}, birthYear=$birthYear, age=$age"
        )

        // 3-3) 몇 시간 전 지원 (announcement_senior.created_at 기준)
        val appliedHoursAgo = applyTimeMap[row.username]?.let { createdAt ->
            val diff = ChronoUnit.HOURS.between(createdAt, now).toInt().coerceAtLeast(0)
            Log.d(
                TAG,
                "[매핑] username=${row.username} createdAt=$createdAt, now=$now, diffHours=$diff"
            )
            diff
        } ?: run {
            Log.w(TAG, "[매핑] username=${row.username} 에 대한 applyTimeMap 이 없어 appliedHoursAgo=0 으로 처리")
            0
        }

        // 3-4) activity_level -> 메달 리소스 매핑
        val medalRes = when (row.activityLevel) {
            3 -> R.drawable.blue_medal
            2 -> R.drawable.yellow_medal
            1 -> R.drawable.red_medal
            else -> {
                Log.w(
                    TAG,
                    "[매핑] username=${row.username} 알 수 없는 activity_level=${row.activityLevel}, 기본 blue_medal 사용"
                )
                R.drawable.blue_medal
            }
        }

        val ui = ApplicantUi(
            name = row.name,
            jobTitle = "직원",              // TODO: 필요하면 great_user_view에 직무 컬럼 추가해서 쓰기
            experience = experienceText,    // ✅ 경력 로직 적용
            location = row.region,
            appliedHoursAgo = appliedHoursAgo,
            medalRes = medalRes,
            age = age
        )

        Log.d(TAG, "[매핑] 최종 ApplicantUi = $ui")

        ui
    }

    Log.d(TAG, "===== getRecentApplicantsByCompany 종료, 결과 size=${result.size} =====")
    result
}

package com.example.dodojob.data.career

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CareerRepositoryImpl(
    private val client: SupabaseClient
) {
    suspend fun list(username: String): List<CareerModels> =
        client.from("career_senior")
            .select { filter { eq("username", username) } }
            .decodeList()

    suspend fun add(
        username: String,
        title: String,
        company: String,
        startDate: String,
        endDate: String,
        description: String?
    ) {
        val row = CareerModels(
            username = username,
            title = title.ifBlank { null },
            company = company.ifBlank { null },
            startDate = startDate.ifBlank { null },
            endDate = endDate.ifBlank { null },
            description = description?.ifBlank { null }
        )
        client.from("career_senior").insert(row)
    }

    fun calculateCareerPeriod(start: String?, end: String?): Pair<Int, Int> {
        if (start.isNullOrBlank() || end.isNullOrBlank()) return 0 to 0

        return try {
            val fmt = DateTimeFormatter.ofPattern("yyyy.MM")
            val startDate = LocalDate.parse("${start}.01", DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val endDate = LocalDate.parse("${end}.01", DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val months = ChronoUnit.MONTHS.between(startDate, endDate).toInt().coerceAtLeast(0)
            val years = months / 12
            val remain = months % 12
            years to remain
        } catch (_: Exception) {
            0 to 0
        }
    }

    /** username의 모든 career 합산 (총 경력) */
    suspend fun totalCareerPeriod(username: String): Pair<Int, Int> {
        val careers = client.from("career_senior")
            .select { filter { eq("username", username) } }
            .decodeList<CareerModels>()

        var totalMonths = 0
        for (c in careers) {
            val (y, m) = calculateCareerPeriod(c.startDate, c.endDate)
            totalMonths += y * 12 + m
        }

        val totalYears = totalMonths / 12
        val remainMonths = totalMonths % 12
        return totalYears to remainMonths
    }

    /** 보기 좋은 문자열 버전 */
    fun formatCareerPeriod(years: Int, months: Int): String {
        return when {
            years == 0 && months == 0 -> "0개월"
            years == 0 -> "${months}개월"
            months == 0 -> "${years}년"
            else -> "${years}년 ${months}개월"
        }
    }



}


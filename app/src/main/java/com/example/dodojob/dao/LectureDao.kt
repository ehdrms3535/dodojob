package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/* ─────────────────────────
 * 1) Lecture 단일 행(DTO)
 * ───────────────────────── */
@Serializable
data class LectureRow(
    val id: Long,
    val title: String? = null,
    val explain: String? = null,
    val category: String? = null,
    val url: String? = null,
    val thumbnail: String? = null
)

/* ─────────────────────────
 * 2) Assign + Lecture 조인 DTO
 *    (lecture_assign_user 의 buy/favorite + 참조 lecture)
 * ───────────────────────── */
@Serializable
data class LectureLiteDto(
    val id: Long,
    val title: String? = null,
    val explain: String? = null,
    val thumbnail: String? = null,
    val url: String? = null
)

@Serializable
data class LectureAssignUserRow(
    val buy: Boolean? = null,
    val favorite: Boolean? = null,
    @SerialName("last_position_ms")
    val lastPositionMs: Long? = null,
    val lecture: LectureLiteDto? = null
)

/* ─────────────────────────
 * 3) 강의 전체 조회
 * ───────────────────────── */
suspend fun fetchLectures(
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY,
    limit: Int? = null
): List<LectureRow> {
    val url = "$supabaseUrl/rest/v1/lecture"
    return http.get(url) {
        parameter("select", "id,title,explain,category,url,thumbnail")
        if (limit != null) parameter("limit", limit)
        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()
}

/* ─────────────────────────
 * 4) 사용자별 배정 강의 + 상태 (buy/favorite/last_position) 조회
 * ───────────────────────── */
suspend fun fetchAssignedCourses(
    username: String?,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): List<LectureAssignUserRow> {
    val url = "$supabaseUrl/rest/v1/lecture_assign_user"
    return http.get(url) {
        // last_position_ms 컬럼까지 함께 조회
        parameter(
            "select",
            "buy,favorite,last_position_ms,lecture:lecture(id,title,explain,thumbnail,url)"
        )
        parameter("user", "eq.$username")
        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()
}

@Serializable
data class LectureAssignUserInsert(
    val user: String?,
    val lecture: Long,
    val buy: Boolean? = null,
    val favorite: Boolean? = null,
    @SerialName("last_position_ms")
    val lastPositionMs: Long? = null
)

/* ─────────────────────────
 * 5) 사용자별 배정 강의 + 상태 (buy/favorite/last_position) upsert
 *    - 필요한 필드만 보내는 방식(기본값 null은 직렬화에서 빠지게 설정돼있다고 가정)
 * ───────────────────────── */
suspend fun upsertLectureAssignUser(
    username: String?,
    lectureId: Long,
    buy: Boolean? = null,
    favorite: Boolean? = null,
    lastPositionMs: Long? = null,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
) {
    val url = "$supabaseUrl/rest/v1/lecture_assign_user"

    http.post(url) {
        // upsert 설정
        header("apikey", token)
        header("Authorization", "Bearer $token")
        header("Prefer", "resolution=merge-duplicates")   // 충돌시 merge
        parameter("on_conflict", "user,lecture")          // unique (user, lecture)

        contentType(ContentType.Application.Json)
        setBody(
            LectureAssignUserInsert(
                user = username,
                lecture = lectureId,
                buy = buy,
                favorite = favorite,
                lastPositionMs = lastPositionMs
            )
        )
    }
}

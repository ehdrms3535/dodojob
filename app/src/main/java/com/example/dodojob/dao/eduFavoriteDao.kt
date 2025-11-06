package com.example.dodojob.dao

import com.example.dodojob.BuildConfig
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import io.ktor.client.call.body

@Serializable
private data class LectureId(val id: Long)

@Serializable
private data class FavoriteRow(val lecture: LectureId? = null)

@Serializable
private data class AssignUpsertBody(
    val user: String,
    val lecture: Long,
    val favorite: Boolean
)


/** 즐겨찾기 ID 목록 조회: lecture_assign_user.favorite = true */
suspend fun fetchFavoriteIdsForUser(
    username: String,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Set<Long> {
    val url = "$supabaseUrl/rest/v1/lecture_assign_user"
    val rows: List<FavoriteRow> = http.get(url) {
        url {
            parameters.append("select", "lecture(id)")
            parameters.append("user", "eq.$username")
            parameters.append("favorite", "eq.true")
        }
        header("apikey", token)
        header("Authorization", "Bearer $token")
    }.body()

    return rows.mapNotNull { it.lecture?.id }.toSet()
}

/** 즐겨찾기 업서트 (없으면 생성, 있으면 갱신) */
suspend fun setFavoriteForUser(
    username: String,
    lectureId: Long,
    favorite: Boolean,
    supabaseUrl: String = BuildConfig.SUPABASE_URL,
    token: String = BuildConfig.SUPABASE_ANON_KEY
): Boolean {
    val url = "$supabaseUrl/rest/v1/lecture_assign_user"
    val body = listOf(AssignUpsertBody(username, lectureId, favorite))

    val resp = http.post(url) {
        url {
            // (user,lecture) 유니크 키 기준 업서트
            parameters.append("on_conflict", "user,lecture")
        }
        header("apikey", token)
        header("Authorization", "Bearer $token")
        header("Prefer", "resolution=merge-duplicates,return=minimal")
        contentType(ContentType.Application.Json)
        setBody(body)
    }
    return resp.status.value in 200..299
}

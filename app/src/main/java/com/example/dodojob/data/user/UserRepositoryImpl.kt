package com.example.dodojob.data.user

import com.example.dodojob.session.CurrentUser
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate


fun rrnToBirthDate(rrnFront: String, rrnBackFirst: String): LocalDate {
    require(rrnFront.length == 6 && rrnBackFirst.length == 1)
    val yy = rrnFront.substring(0, 2).toInt()
    val mm = rrnFront.substring(2, 4).toInt()
    val dd = rrnFront.substring(4, 6).toInt()
    val century = when (rrnBackFirst[0]) {
        '1','2','5','6' -> 1900   // 내국인/외국인 1900~1999
        '3','4','7','8' -> 2000   // 내국인/외국인 2000~2099
        else -> error("지원하지 않는 주민번호 체계입니다.")
    }
    return LocalDate.of(century + yy, mm, dd) // 유효성 자동검사
}

@Serializable
data class UserRow(
    val id: String,
    val name: String,
    val gender: String,
    val birthdate: String,
    val region: String,
    val phone: String,
    val email: String,
    val username: String,
    val password: String,
    val job: String
)

class UserRepositorySupabase(
    private val client: io.github.jan.supabase.SupabaseClient
) : UserRepository {

    override suspend fun insertUser(user: UserDto) {
        val birth = rrnToBirthDate(user.rrnFront, user.rrnBackFirst).toString() // "YYYY-MM-DD"
        client.from("users_tmp").upsert(
            UserRow(
                id = user.id,
                name = user.name,
                gender = user.gender,
                birthdate = birth,
                region = user.region,
                phone = user.phone,
                email = user.email,
                username = user.username,
                password = user.password, // 실제로는 해시 or GoTrue 사용
                job = user.job
            )
        ){
            onConflict = "id"   // ⚡ PK/Unique 컬럼 지정
        }
    }

    override suspend fun upsertIdPw(
        id: String?,                 // ❗ null 아님
        username: String,
        password: String
    ) {
        client.from("users_tmp").upsert(
            mapOf(
                "id" to id,         // ❗ 반드시 포함
                "username" to username,
                "password" to password,
            )
        ) {
            onConflict = "id"
            ignoreDuplicates = false   // 충돌 시 병합(UPDATE)
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        val username = CurrentUser.username
            ?: error("로그인 정보가 없습니다.")

        val users = client.from("users_tmp").select {
            filter {
                eq("username", username)
                eq("password", currentPassword)
            }
            limit(1)
        }.decodeList<JsonObject>()

        if (users.isEmpty()) {
            error("현재 비밀번호가 올바르지 않습니다.")
        }

        client.from("users_tmp").update(
            mapOf("password" to newPassword)
        ) {
            filter { eq("username", username) }
        }

        CurrentUser.setLogin(username, newPassword)
    }
}
/*
카카오 주식회사
디지털 콘텐츠
2013.01.01 ~ 2025.09.26
 */
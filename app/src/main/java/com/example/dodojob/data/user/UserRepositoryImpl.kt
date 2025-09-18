package com.example.dodojob.data.user

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
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

    override suspend fun upsertIdPwByPhone(
        username: String,
        rawPassword: String
    ) {
        // ✅ phone UNIQUE 기준으로 충돌 시 username/password만 갱신
        client.from("users_tmp").upsert(
            mapOf(
                "username" to username,
                "password" to rawPassword,
            )
        ) {
            ignoreDuplicates = false   // 충돌 시 UPDATE(merge)
        }
    }
}
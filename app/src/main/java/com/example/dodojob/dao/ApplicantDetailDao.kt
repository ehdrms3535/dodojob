package com.example.dodojob.dao

import android.util.Log
import com.example.dodojob.BuildConfig
import com.example.dodojob.data.career.CareerModels
import com.example.dodojob.data.license.LicenseModels
import com.example.dodojob.ui.feature.employ.UserTmpRow
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/* ===== Supabase Base 설정 ===== */
private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
private const val SUPABASE_KEY = BuildConfig.SUPABASE_ANON_KEY

/* ===== API 함수 ===== */

suspend fun fetchUserTmp(username: String): UserTmpRow? {
    return try {
        val list: List<UserTmpRow> = http.get("$SUPABASE_URL/rest/v1/users_tmp") {
            parameter("username", "eq.$username")
            parameter("select", "username,name,gender,birthdate,region,phone,email")
            header("apikey", SUPABASE_KEY)
            header("Authorization", "Bearer $SUPABASE_KEY")
        }.body()

        list.firstOrNull()
    } catch (e: Exception) {
        Log.e("ApplicantInfo", "[REST] users_tmp 조회 오류", e)
        null
    }
}

suspend fun fetchCareers(username: String): List<CareerModels> {
    return try {
        http.get("$SUPABASE_URL/rest/v1/career_senior") {
            parameter("username", "eq.$username")
            parameter(
                "select",
                "username,company,career_title,start_date,end_date,description,id"
            )
            header("apikey", SUPABASE_KEY)
            header("Authorization", "Bearer $SUPABASE_KEY")
        }.body()
    } catch (e: Exception) {
        Log.e("ApplicantInfo", "[REST] career_senior 조회 오류", e)
        emptyList()
    }
}

suspend fun fetchLicenses(username: String): List<LicenseModels> {
    return try {
        http.get("$SUPABASE_URL/rest/v1/license_senior") {
            parameter("username", "eq.$username")
            parameter(
                "select",
                "username,license_name,license_location,license_number,id"
            )
            header("apikey", SUPABASE_KEY)
            header("Authorization", "Bearer $SUPABASE_KEY")
        }.body()
    } catch (e: Exception) {
        Log.e("ApplicantInfo", "[REST] license_senior 조회 오류", e)
        emptyList()
    }
}

suspend fun fetchJobtype(username: String): JobtypeRow? {
    return try {
        val list: List<JobtypeRow> = http.get("$SUPABASE_URL/rest/v1/jobtype") {
            parameter("id", "eq.$username")
            parameter("select", "id,job_talent,job_manage,job_service,job_care")
            header("apikey", SUPABASE_KEY)
            header("Authorization", "Bearer $SUPABASE_KEY")
        }.body()

        list.firstOrNull()
    } catch (e: Exception) {
        Log.e("ApplicantInfo", "[REST] jobtype 조회 오류", e)
        null
    }
}


@Serializable
data class UserImageRow(
    val id: String,
    @SerialName("img_url") val imgUrl: String,
    @SerialName("user_imform") val userImform: String? = null
)

suspend fun fetchUserImage(username: String): UserImageRow? {
    return try {
        val list: List<UserImageRow> =
            http.get("$SUPABASE_URL/rest/v1/user_image") {
                parameter("id", "eq.$username")
                parameter("select", "id,img_url,user_imform")
                header("apikey", SUPABASE_KEY)
                header("Authorization", "Bearer $SUPABASE_KEY")
            }.body()

        list.firstOrNull()
    } catch (e: Exception) {
        Log.e("ApplicantInfo", "[REST] user_image 조회 오류", e)
        null
    }
}

package com.example.dodojob.data.application

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.postgrest

data class ApplicationData(
    val orgName: String?,      // announcement.company_name
    val taskMajor: String?,    // working_conditions.major
    val userName: String?,     // users_tmp.name
    val userPhone: String?,    // users_tmp.phone
    val careerTitle: String?   // career_senior.career_title
)

class ApplyRepository(
    private val client: SupabaseClient
) {

    @Serializable
    private data class AnnouncementRow(
        val id: Long,
        val company_name: String? = null
    )

    @Serializable
    private data class WorkingRow(
        val id: Long,
        val major: String? = null
    )

    @Serializable
    private data class UserRow(
        val username: String,
        val name: String? = null,
        val phone: String? = null
    )

    @Serializable
    private data class CareerRow(
        val id: Long,
        val username: String,
        val career_title: String? = null
    )

    suspend fun loadApplicationData(
        announcementId: Long,
        username: String?
    ): ApplicationData {
        require(!username.isNullOrBlank()) {
            "username is null or blank in loadApplicationData"
        }

        val announcementList = client
            .from("announcement")
            .select()
            .decodeList<AnnouncementRow>()

        val announcement = announcementList.firstOrNull { it.id == announcementId }
            ?: error("Announcement not found for id=$announcementId")

        val workingList = client
            .from("working_conditions")
            .select()
            .decodeList<WorkingRow>()

        val working = workingList.firstOrNull { it.id == announcementId }
            ?: error("Working condition not found for id=$announcementId")

        val userList = client
            .from("users_tmp")
            .select()
            .decodeList<UserRow>()

        val user = userList.firstOrNull { it.username == username }
            ?: error("User not found for username=$username")

        val careerList = client
            .from("career_senior")
            .select()
            .decodeList<CareerRow>()

        val career = careerList
            .filter { it.username == username }
            .maxByOrNull { it.id }

        return ApplicationData(
            orgName = announcement.company_name,
            taskMajor = working.major,
            userName = user.name,
            userPhone = user.phone,
            careerTitle = career?.career_title
        )
    }

    @Serializable
    private data class ApplicationInsert(
        val announcement_id: Long,
        val senior_username: String,
        val health_condition: String
    )

    suspend fun submitApplication(
        announcementId: Long,
        username: String?,
        healthCondition: String
    ) {
        require(!username.isNullOrBlank()) { "username is null or blank" }

        val body = ApplicationInsert(
            announcement_id = announcementId,
            senior_username = username,
            health_condition = healthCondition
        )

        client.postgrest["application"].insert(body)
    }
}

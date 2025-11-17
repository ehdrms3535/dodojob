package com.example.dodojob.data.senior

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserImage(
    @SerialName("img_url")
    val imgUrl: String? = null
)


data class SeniorDto(
    val id: String,
    val name: String,
    val gender: String,
    val phone: String,
    val email: String,
    val password: String,
    val applyCount: Long,
    val resumeViews: Long,
    val recentCount: Long,
    val likedCount: Long,
    val activityLevel: Long
)


@Serializable
data class UserTmpLite(
    val name: String? = null,
    val gender: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val password: String? = null,
    val username: String? = null,
    @SerialName("created_at") val created_at: String? = null,
    @SerialName("user_image")
    val userImage: UserImage? = null
)


@Serializable
data class SeniorJoined(
    val username: String,
    val applyCount: Long = 0,
    val resumeViews: Long = 0,
    val recentCount: Long = 0,
    val likedCount: Long = 0,
    val activityLevel: Long = 1,
    val applyWithinYear: Long = 0,
    val realWorkExpCount: Long = 0,
    val eduCompleted: Boolean = false,
    @SerialName("users_tmp") val user: UserTmpLite? = null
)

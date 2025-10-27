package com.example.dodojob.data.career

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CareerModels(
    val id: Long? = null,
    val username: String,
    @SerialName("career_title") val title: String? = null,     // 직무/직책
    @SerialName("company") val company: String? = null,        // 회사/기관
    @SerialName("start_date") val startDate: String? = null,   // YYYY.MM / YYYY-MM
    @SerialName("end_date") val endDate: String? = null,       // 동일 형식
    @SerialName("description") val description: String? = null // 선택
)

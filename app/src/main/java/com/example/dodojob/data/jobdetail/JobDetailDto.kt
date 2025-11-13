package com.example.dodojob.data.jobdetail

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobDetailDto(
    val id: Long,
    val title: String? = null,

    @SerialName("company_id")     val companyId: String? = null,
    @SerialName("company_name")   val companyName: String? = null,
    @SerialName("company_locate") val companyLocate: String? = null,

    // 급여
    @SerialName("pay_type")   val payType: String? = null,
    @SerialName("pay_amount") val payAmount: Long? = null,
    @SerialName("pay_text")   val payText: String? = null,

    // 시간
    @SerialName("time_start")           val timeStart: String? = null,
    @SerialName("time_end")             val timeEnd: String? = null,
    @SerialName("time_text")            val timeText: String? = null,
    @SerialName("work_duration_minutes") val workDurationMinutes: Int? = null,
    @SerialName("work_duration_text")    val workDurationText: String? = null,

    // 요일
    @SerialName("week_bits")       val weekBits: String? = null,
    @SerialName("week_days_count") val weekDaysCount: Int? = null,
    @SerialName("week_text")       val weekText: String? = null,

    // 기타
    @SerialName("career_text")       val careerText: String? = null,
    @SerialName("career_years")      val careerYears: Int? = null,
    @SerialName("career")
    val benefit: String? = null,
    @SerialName("benefits") val benefits: List<String> = emptyList(),
    @SerialName("recruitment_period") val recruitmentPeriod: String? = null,
    val duties: String? = null,

    // 이미지
    @SerialName("image") val imageUrl: String? = null,

    // 좋아요
    @SerialName("isliked") val isLiked: Boolean = false
)

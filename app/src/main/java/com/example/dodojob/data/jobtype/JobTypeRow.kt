// com/example/dodojob/data/jobtype/JobTypeRow.kt
package com.example.dodojob.data.jobtype

import kotlinx.serialization.Serializable

@Serializable
data class JobTypeRow(
    val id: String?,                // username (FK)
    val jobtype: String,        // 전체/대표 마스크 (필요 없으면 생략)
    val locate: String? = "0",
    val job_talent: String,
    val job_manage: String,
    val job_service: String,
    val job_care: String,
    val term: String?,
    val days: Boolean,
    val weekend: Boolean,
    val week: String,
    val time: Boolean
)

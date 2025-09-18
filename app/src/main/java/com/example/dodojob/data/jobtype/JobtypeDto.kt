package com.example.dodojob.data.jobtype

data class JobtypeDto(
    val id: String?,
    val jobtype: String,
    val locate: String?,
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

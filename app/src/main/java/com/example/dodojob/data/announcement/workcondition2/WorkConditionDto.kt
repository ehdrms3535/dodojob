package com.example.dodojob.data.announcement.workcondition2

import com.example.dodojob.ui.feature.employ.FakeTalentRepo

data class WorkConditionDto(
    val category : String,
    val talent: String,
    val major : String,
    val form : String,
    val weekok : String,
    val starttime: String,
    val endtime : String,
    val intensity : String
)

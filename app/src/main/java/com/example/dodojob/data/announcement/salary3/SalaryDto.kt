package com.example.dodojob.data.announcement.salary3

data class SalaryDto (
    val id : Long,
    val salary_type : String, // 급여정보
    val salary_amount : Long, // 시급
    val benefit : String, //복리혜택
    val career : String, //경력요구사항
    val gender : String // 성별
)

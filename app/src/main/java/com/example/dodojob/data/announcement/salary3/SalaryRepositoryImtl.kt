package com.example.dodojob.data.announcement.salary3

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable


@Serializable
data class SalaryRow(
    val id_salary : Long? = null,
    val id : Long? = null,
    val salary_type : String,
    val salary_amount : Long,
    val benefit : String,
    val gender : String,
    val career : String
)


class SalaryRepositorySupabase(
    private val client: SupabaseClient
) : SalaryRepository {
    override suspend fun insertSalary(announcement: SalaryDto) {
        client.from("salary_condition").insert(
            SalaryRow(
                id=announcement.id,
                salary_type = announcement.salary_type,
                salary_amount = announcement.salary_amount,
                benefit = announcement.benefit,
                gender = announcement.gender,
                career = announcement.career
            )

        )
    }
}
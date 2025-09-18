package com.example.dodojob.data.employ

import com.example.dodojob.data.employ.EmployDto
import com.example.dodojob.data.employ.EmployRepository
import io.github.jan.supabase.postgrest.from
import com.example.dodojob.util.Bits
import kotlinx.serialization.Serializable

@Serializable
data class EmployRow(
    val id: String,
    val companyid: String,
)

class EmployRepositorySupabase(
    private val client: io.github.jan.supabase.SupabaseClient
) : EmployRepository {

    override suspend fun insertEmploy(employ: EmployDto) {

        client.from("employ_tmp").insert(
            EmployRow(
                id = employ.id,
                companyid = employ.companyid
            )
        )
    }
}
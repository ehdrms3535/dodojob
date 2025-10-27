package com.example.dodojob.data.career

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class CareerRepositoryImpl(
    private val client: SupabaseClient
) {
    suspend fun list(username: String): List<CareerModels> =
        client.from("career_senior")
            .select { filter { eq("username", username) } }
            .decodeList()

    suspend fun add(
        username: String,
        title: String,
        company: String,
        startDate: String,
        endDate: String,
        description: String?
    ) {
        val row = CareerModels(
            username = username,
            title = title.ifBlank { null },
            company = company.ifBlank { null },
            startDate = startDate.ifBlank { null },
            endDate = endDate.ifBlank { null },
            description = description?.ifBlank { null }
        )
        client.from("career_senior").insert(row)
    }
}

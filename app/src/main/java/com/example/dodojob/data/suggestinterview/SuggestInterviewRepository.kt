package com.example.dodojob.data.suggestinterview

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class SuggestInterviewInsert(
    val announcement_id: Long?,
    val username: String?,
    val interview_date: String?,
    val interview_time: String?,
    val method: String?,
    val address: String?,
    val address_detail: String?,
    val note: String?
)

class SuggestInterviewRepository(private val client: SupabaseClient) {

    suspend fun insert(row: SuggestInterviewInsert) {
        client.from("suggest_interview")
            .insert(row)
    }
}

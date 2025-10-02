package com.example.dodojob.data.senior

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
private data class SeniorRow(
    val username: String,
    val applyCount: Long = 0,
    val resumeViews: Long = 0,
    val recentCount: Long = 0,
    val likedCount: Long = 0,
    val activityLevel: Long = 1,
    val applyWithinYear: Long = 0,
    val realWorkExpCount: Long = 0,
    val eduCompleted: Boolean = false
)

class SeniorRepositorySupabase(
    private val client: SupabaseClient
) : SeniorRepository {

    override suspend fun insertSenior(senior: SeniorDto) {
        client.from("senior").insert(
            SeniorRow(
                username = senior.id,
                applyCount = senior.applyCount,
                resumeViews = senior.resumeViews,
                recentCount = senior.recentCount,
                likedCount = senior.likedCount,
                activityLevel = senior.activityLevel,
            )
        )
    }

    override suspend fun upsertSenior(username: String) {
        client.from("Senior").upsert(
            SeniorRow(username = username)
        ) {
            onConflict = "username"
            ignoreDuplicates = false
        }
    }
}

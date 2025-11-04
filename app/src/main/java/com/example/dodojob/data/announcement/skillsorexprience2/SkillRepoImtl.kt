package com.example.dodojob.data.announcement.skillsorexprience2

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class SkillRow(
    val id_skill : Long? = null,
    val id : Long? = null,
    val skill : String
)


class SkillRepoImtlSupabase(
private val client: SupabaseClient
) : SkillRepo{
    override suspend fun insertSkill(announcement: SkillDto) {
        client.from("skills_or_experience").insert(
            SkillRow(
                id =  announcement.id,
                skill = announcement.skill
            )
        )
    }
}
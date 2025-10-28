package com.example.dodojob.data.announcement.workcondition2

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class WorkconditionRow(
    val id : Long? = null,
    val category : String,
    val talent : String,
    val major  : String,
    val form : String,
    val weekok : String,
    val starttime : String,
    val endtime : String,
    val intensity : String
)

class WorkconditionSupabase(
    private val client: SupabaseClient)
    : WorkconditionRepo{
    override suspend fun insertWorkcondition(announcement: WorkConditionDto) {
        client.from("").insert(
            WorkconditionRow(
                category =announcement.category,
                talent = announcement.talent,
                major = announcement.major,
                form = announcement.form,
                weekok = announcement.weekok,
                starttime = announcement.starttime,
                endtime = announcement.endtime,
                intensity = announcement.intensity
            )
        )
    }
}
package com.example.dodojob.data.announcement.preferential3

import com.example.dodojob.data.announcement.needlicense3.NeedlicenseDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class PreferntialRow(
    val id : Long? = null,
    val announcement_id : Long? =null,
    val preferential_treatment : String
)


class PreferentialRepoSuSupabase(
    private val client: SupabaseClient
) : PreferentialRepo  {

    override suspend fun insertPreferential(announcement: PreferentialDto){
        client.from("preferential_treatment").insert(
            PreferntialRow(
                announcement_id = announcement.id,
                preferential_treatment = announcement.preferential_treatment
            )
        )
    }
}
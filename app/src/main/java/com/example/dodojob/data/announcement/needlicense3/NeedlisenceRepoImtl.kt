package com.example.dodojob.data.announcement.needlicense3

import com.example.dodojob.data.employ.EmployRow
import kotlinx.serialization.Serializable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

@Serializable
data class needlicenseRow(
    val id_license: Long? = null,              // 자동 증가 PK
    val id: Long? = null,
    val need1: String,
    )


class NeedlisenceRepoSupabase(
    private val client: SupabaseClient
) : NeedlicenseRepo {

    /** announcement insert 후 자동증가 id 반환 */
    override suspend fun insertNeedlisence(announcement: NeedlicenseDto) {
        client.from("license_announcement").insert(
            needlicenseRow(
                id = 12,
                need1   = announcement.need1
            )
        )

    }

}

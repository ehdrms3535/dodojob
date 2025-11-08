package com.example.dodojob.data.greatuser

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class SGURow(
    val employ: String,
    val senior: String,
    val isreal: Boolean
)

class ScrappedGreatUserSupabase(
    private val client: io.github.jan.supabase.SupabaseClient
) : ScrappedGreatUserRepo {

    override suspend fun insertSGU(SGU: ScrappedGreatUserDto) {

        client.from("scrappedgreatuser").insert(
            SGURow(
                employ = SGU.employ,
                senior = SGU.senior,
                isreal = SGU.isreal
            )
        )
    }

    override suspend fun deleteSGU(SGU: ScrappedGreatUserDto) {
        client.from("scrappedgreatuser")
            .delete {
                // 예: employ 와 senior 가 일치하는 행 삭제
                filter {
                    eq("employ", SGU.employ)
                    eq("senior", SGU.senior)
                }
            }
    }
}
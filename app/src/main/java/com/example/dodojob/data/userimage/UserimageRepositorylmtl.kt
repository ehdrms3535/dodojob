package com.example.dodojob.data.userimage

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class UserimgRow(
    val id: String?,
    val img_url: String,
    val user_imform: String
)

class UserimageSupabase(
    private val client: io.github.jan.supabase.SupabaseClient
) : UserimageRepository {

    override suspend fun insertUserimage(userimg: UserimageDto) {

        client.from("user_image").insert(
            UserimgRow(
                id = userimg.id,
                img_url = userimg.img_url,
                user_imform = userimg.user_imform
            )
        )
    }
}
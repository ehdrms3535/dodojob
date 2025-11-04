package com.example.dodojob.data.announcement.price4

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class PriceRow(
    val id : Long? = null,
    val price : Boolean,
    val date : Long
)

class PriceRepoSupabase(
    private val client: SupabaseClient
) : PriceRepo {
    override suspend fun insertPrice(announcement : PriceDto){
        client.from("announcement_pricing").insert(
            PriceRow(
                id =  announcement.id,
                price = announcement.price,
                date = announcement.date
            )
        )
    }
}
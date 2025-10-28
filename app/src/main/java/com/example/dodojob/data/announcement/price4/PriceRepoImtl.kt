package com.example.dodojob.data.announcement.price4

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class PriceRow(
    val id_pricing : Long? =null,
    val id : Long? = null,
    val price : Boolean,
    val data : Long
)

class PriceRepoSupabase(
    private val client: SupabaseClient
) : PriceRepo {
    override suspend fun insertPrice(announcement : PriceDto){
        client.from("announcement_pricing").insert(
            PriceRow(
                price = announcement.price,
                data = announcement.date
            )
        )
    }
}
package com.example.dodojob.data.recentwatch

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
private data class RecentWatchRow(
    val seniorid: String,
    val announceid: Long
)

class RecentWatchSupabase(
    private val client: SupabaseClient
) : RecentWatchRepo {

    override suspend fun insertRecentWatch(senior: RecentWatchDto) {
        client.from("recent_watch").insert(
            RecentWatchRow(
                seniorid = senior.seniorid,
                announceid = senior.announceid
            )
        )
    }

    override suspend fun upsertRecentWatch(seniorid: String, announceid: Long) {
        client.from("recent_watch").upsert(
            RecentWatchRow(
                seniorid = seniorid,
                announceid = announceid
            )
        ) {
            // ⚠️ recent_watch 테이블에 UNIQUE(seniorid, announceid) 제약이 반드시 있어야 함
            onConflict = "seniorid,announceid"
            ignoreDuplicates = false   // true면 기존 값 그대로, false면 UPDATE
        }
    }
}

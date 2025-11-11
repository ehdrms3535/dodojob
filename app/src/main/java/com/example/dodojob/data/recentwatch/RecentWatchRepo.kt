package com.example.dodojob.data.recentwatch

interface RecentWatchRepo {
    suspend fun insertRecentWatch(seniorid: RecentWatchDto)
    suspend fun upsertRecentWatch(seniorid: String,announceid: Long)
}

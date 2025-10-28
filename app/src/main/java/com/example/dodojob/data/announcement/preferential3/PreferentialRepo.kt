package com.example.dodojob.data.announcement.preferential3

interface PreferentialRepo {
    suspend fun insertPreferential(announcement: PreferentialDto)
}


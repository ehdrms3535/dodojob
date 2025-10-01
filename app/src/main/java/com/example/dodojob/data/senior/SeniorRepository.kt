package com.example.dodojob.data.senior

interface SeniorRepository {
    suspend fun insertSenior(senior: SeniorDto)
    suspend fun upsertSenior(username: String)
}

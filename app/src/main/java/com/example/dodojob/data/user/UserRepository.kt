package com.example.dodojob.data.user

interface UserRepository {
    suspend fun insertUser(user: UserDto)

    suspend fun upsertIdPw(
        id:String?,
        username: String,
        rawPassword: String
    )
}
package com.example.dodojob.data.user

interface UserRepository {
    suspend fun insertUser(user: UserDto)

    suspend fun upsertIdPw(
        id:String?,
        username: String,
        rawPassword: String
    )

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    )

    suspend fun verifyPassword(
        inputPassword: String
    ): Boolean

}
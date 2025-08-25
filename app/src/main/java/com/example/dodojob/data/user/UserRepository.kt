package com.example.dodojob.data.user

interface UserRepository {
    suspend fun insertUser(user: UserDto)
}
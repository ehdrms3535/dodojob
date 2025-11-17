package com.example.dodojob.data.user

data class UserDto(
    val id: String,
    val name: String,
    val gender: String,
    val rrnFront: String,
    val rrnBackFirst: String,
    val region: String,
    val phone: String,
    val email: String,
    val username: String?,
    val password: String?,
    val job: String
)
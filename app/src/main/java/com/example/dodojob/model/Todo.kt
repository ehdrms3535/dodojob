package com.example.dodojob.model

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    val id: Int,
    val title: String
)
// data/education/Models.kt
package com.example.dodojob.data.education

import kotlinx.serialization.Serializable

@Serializable
data class LectureRow(
    val id: Long,
    val title: String? = null,
    val explain: String? = null,
    val category: String? = null,
    val url: String? = null,
    val thumbnail: String? = null
)

@Serializable
data class LectureAssignUserRow(
    val lecture: LectureRow? = null
)

data class Course(
    val id: Long,
    val title: String,
    val sub: String?,
    val imageUrl: String?,
    val buy: Boolean?,        // 👈
    val favorite: Boolean?    // 👈
)


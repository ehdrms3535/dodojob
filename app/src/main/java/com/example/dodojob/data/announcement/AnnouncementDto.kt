package com.example.dodojob.data.announcement

data class AnnouncementDto (
    val company_name: String,
    val public: Boolean,
    val company_id: String?=null,
    val company_locate: String,
    val detail_locate: String
)


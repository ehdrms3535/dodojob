package com.example.dodojob.data.session

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CompanyIdModel(
    @SerialName("companyid") val companyid: String
)

@Serializable
data class UserEmailModel(
    val email: String
)
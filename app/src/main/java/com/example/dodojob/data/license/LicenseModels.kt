package com.example.dodojob.data.license

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LicenseModels(
    val id: Long? = null,
    val username: String,
    @SerialName("license_name") val name: String? = null,
    @SerialName("license_location") val location: String? = null,
    @SerialName("license_number") val number: String? = null
)
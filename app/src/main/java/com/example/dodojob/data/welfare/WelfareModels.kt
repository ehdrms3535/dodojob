package com.example.dodojob.data.welfare

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/* ========== DB Rows ========== */
@Serializable
data class WelfareRow(
    val id: Long,
    @SerialName("welfare_name") val title: String,
    @SerialName("welfare_locate") val agency: String? = null,
    /** false=HEALTH, true=LEISURE, null=미지정 */
    val type: Boolean? = null,
    @SerialName("explain") val description: String? = null,
    /** UI의 supportType 로 사용 */
    val offer: String? = null
)

@Serializable
data class WelfareSeniorRow(
    val id: Long? = null,
    val username: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("welfare_status") val status: WelfareStatus? = null,
    @SerialName("welfare_id") val welfareId: Long,
    @SerialName("welfare") val welfare: WelfareRow? = null
)

@Serializable
data class Welfare(
    val id: Long,
    val title: String,
    val agency: String?,
    val category: WelfareCategory?, // HEALTH / LEISURE / null
    val description: String?,
    val supportType: String?
)

@Serializable
data class WelfareApplication(
    val id: Long?,                 // welfare_senior.id
    val welfareId: Long,
    val username: String,
    val status: WelfareStatus,
    val createdAt: String? = null
)


@Serializable
enum class WelfareStatus {
    @SerialName("review")
    @JsonNames("REVIEW", "Review")
    REVIEW,

    @SerialName("approved")
    @JsonNames("APPROVED", "Approved")
    APPROVED,

    @SerialName("pending")
    @JsonNames("PENDING", "Pending")
    PENDING
}


enum class WelfareCategory { HEALTH, LEISURE }


fun Boolean?.toCategory(): WelfareCategory? = when (this) {
    false -> WelfareCategory.HEALTH
    true  -> WelfareCategory.LEISURE
    else  -> null
}
fun WelfareCategory.toDbBoolean(): Boolean = when (this) {
    WelfareCategory.HEALTH  -> false
    WelfareCategory.LEISURE -> true
}

fun WelfareRow.toDomain(): Welfare = Welfare(
    id          = id,
    title       = title,
    agency      = agency,
    category    = type.toCategory(), // false→HEALTH, true→LEISURE
    description = description,
    supportType = offer
)

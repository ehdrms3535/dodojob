import kotlinx.serialization.Serializable

@Serializable
data class ApplicantRow(
    val id: Long,
    val name: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val headline: String? = null,
    val address: String? = null,
    val careerYears: Int? = null,
    val method: String? = null,
    val postingTitle: String? = null,
    val status: String? = null,
    val activityLevel: Int? = null
)

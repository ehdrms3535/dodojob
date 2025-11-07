package com.example.dodojob.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.dodojob.data.career.CareerModels
import com.example.dodojob.data.license.LicenseModels
import com.example.dodojob.data.greatuser.GreatUser

// ✅ 자격증 (License) 데이터 클래스
@Serializable
data class LicenseModels(
    val id: Long? = null,
    val username: String,
    @SerialName("license_name") val name: String? = null,
    @SerialName("license_location") val location: String? = null,
    @SerialName("license_number") val number: String? = null
)

// ✅ 경력 (Career) 데이터 클래스
@Serializable
data class CareerModels(
    val id: Long? = null,
    val username: String,
    @SerialName("career_title") val title: String? = null,     // 직무/직책
    @SerialName("company") val company: String? = null,        // 회사/기관
    @SerialName("start_date") val startDate: String? = null,   // YYYY.MM / YYYY-MM
    @SerialName("end_date") val endDate: String? = null,       // 동일 형식
    @SerialName("description") val description: String? = null // 선택
)

@Serializable
data class GreatUser(
    val name: String? = null,
    val gender : String? = null,
    // Supabase date → 문자열로 먼저 받는 게 가장 안전
    val birthdate : String? = null,
    val region : String? = null,
    val phone : String? = null,
    val email : String? = null,
    val username : String? = null,
    val password : String? = null,
    val job : String? = null,

    val jobtype : String? = null,
    val locate : String? = null,
    val job_talent : String? = null,
    val job_manage : String? = null,
    val job_service : String? = null,
    val job_care : String? = null,
    val term : String? = null,
    val days : Boolean? = null,
    val weekend : Boolean? = null,
    val week : String? = null,
    val time : Boolean? = null,

    val license_name : String? = null,
    val license_location : String? = null,
    val license_number : String? = null,

    val company : String? = null,
    val career_title : String? = null,
    val start_date : String? = null,
    val end_date : String? = null,
    val description : String? = null,

    val applyCount : Long? = null,
    val resumeViews : Long? = null,
    val recentCount : Long? = null,
    val likedCount : Long? = null,
    val activityLevel : Long? = null,     // 뷰에서 camelCase로 나가면 그대로, snake면 activity_level로 필드명을 맞추세요
    val applyWithinYear : Long? = null,
    val realWorkExpCount : Long? = null,
    val eduCompleted : Boolean? = null
)

// ✅ 유저 전역 상태 저장소
object GreatUserView {

    private val _id = MutableStateFlow<String?>(null)           // Supabase auth user id (UUID)
    private val _username = MutableStateFlow<String?>(null)     // 로그인 아이디/이메일
    private val _licenses = MutableStateFlow<List<LicenseModels>>(emptyList())  // ✅ 여러 개 자격증
    private val _careers = MutableStateFlow<List<CareerModels>>(emptyList())    // ✅ 여러 개 경력
    private val _greatuser = MutableStateFlow<GreatUser?>(null)


    val idFlow: StateFlow<String?> get() = _id
    val usernameFlow: StateFlow<String?> get() = _username

    val id: String? get() = _id.value
    val username: String? get() = _username.value
    val licenses: List<LicenseModels> get() = _licenses.value
    val careers: List<CareerModels> get() = _careers.value
    val greatuser: GreatUser? get() = _greatuser.value
    // --- Setter ---
    fun setId(id: String?) { _id.value = id }
    fun setUsername(username: String?) { _username.value = username }

    fun setLicenses(list: List<LicenseModels>) { _licenses.value = list }
    fun addLicense(item: LicenseModels) { _licenses.value = _licenses.value + item }

    fun setCareers(list: List<CareerModels>) { _careers.value = list }
    fun addCareer(item: CareerModels) { _careers.value = _careers.value + item }

    fun setGreatuser(list: GreatUser?) { _greatuser.value = list }
    // --- 초기화 ---
    fun clear() {
        _id.value = null
        _username.value = null
        _licenses.value = emptyList()
        _careers.value = emptyList()
        _greatuser.value = null
    }
}

package com.example.dodojob.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CurrentUser {
    private val _id = MutableStateFlow<String?>(null)           // Supabase auth user id (UUID)
    private val _username = MutableStateFlow<String?>(null)     // 로그인 아이디/이메일
    private val _pw = MutableStateFlow<String?>(null)           // (필요시만 저장!)
    private val _locate = MutableStateFlow<String?>(null)
    private val _radius = MutableStateFlow<Double>(0.0)
    private val _jobtype = MutableStateFlow<String>("")

    val idFlow: StateFlow<String?> get() = _id
    val usernameFlow: StateFlow<String?> get() = _username

    val id: String? get() = _id.value
    val username: String? get() = _username.value
    val password: String? get() = _pw.value
    val locate: String? get() = _locate.value
    val radius: Double get() = _radius.value
    val jobtype: String get() = _jobtype.value

    /** 로그인 시 (username/password) */
    fun setLogin(username: String, password: String) {
        _username.value = username
        _pw.value = password
    }

    /** 유저 UUID (Supabase auth id) 세팅 */
    fun setAuthUserId(id: String) { _id.value = id }

    fun setLocate(locate: String?, radius: Double) {
        _locate.value = locate
        _radius.value = radius
    }

    fun setUsername(username: String?) { _username.value = username }
    fun setJob(job: String) { _jobtype.value = job }

    fun clear() {
        _id.value = null
        _username.value = null
        _pw.value = null
        _locate.value = null
        _radius.value = 0.0
        _jobtype.value = ""
    }

    fun requireId(): String =
        _id.value ?: throw IllegalStateException("Not logged in: id is null")

    fun requireUsername(): String =
        _username.value ?: throw IllegalStateException("Not logged in: username is null")
}

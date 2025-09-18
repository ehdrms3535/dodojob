package com.example.dodojob.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CurrentUser {
    private val _id = MutableStateFlow<String?>(null)
    private val _username = MutableStateFlow<String?>(null)
    private val _pw = MutableStateFlow<String?>(null)
    private val _locate = MutableStateFlow<String?>(null)
    private val _radium = MutableStateFlow<Double>(0.0)

    val idFlow: StateFlow<String?> get() = _id
    val usernameFlow: StateFlow<String?> get() = _username

    val id: String? get() = _id.value
    val username: String? get() = _username.value
    val password: String? get() = _pw.value
    val locate : String? get() = _locate.value
    val redium : Double get() = _radium.value
    /** 로그인 시 둘 다 세팅 */
    fun setLogin(username: String, password: String) {
        _username.value = username
        _pw.value = password
    }

    fun setlocate(locate : String, radium : Double){
        _locate.value = locate
        _radium.value = radium
    }

    /** 호환용: 예전 코드에서 쓰던 set(id) */
    fun set(id: String) { _id.value = id }

    fun setuser(id: String) { _id.value = username }
    fun setUsername(username: String) { _username.value = username }

    fun clear() { _id.value = null; _username.value = null }

    fun requireId(): String =
        _id.value ?: throw IllegalStateException("Not logged in: id is null")

    fun requireUsername(): String =
        _username.value ?: throw IllegalStateException("Not logged in: username is null")
}

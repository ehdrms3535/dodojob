// SessionViewModel.kt
package com.example.dodojob.session

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionViewModel : ViewModel() {

    // 로그인/세션용 메모리 상태
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _role = MutableStateFlow<String?>(null) // "시니어" / "고용주" 등
    val role: StateFlow<String?> = _role.asStateFlow()

    // 예: 화면 간 임시 선택값도 여기다 둬도 됨
    private val _tempSelections = MutableStateFlow<Map<String, Any?>>(emptyMap())
    val tempSelections: StateFlow<Map<String, Any?>> = _tempSelections.asStateFlow()

    fun setLogin(id: String, name: String, role: String?) {
        _userId.value = id
        _username.value = name
        _role.value = role
    }

    fun setTemp(key: String, value: Any?) {
        _tempSelections.value = _tempSelections.value.toMutableMap().apply { put(key, value) }
    }

    fun setrole(role:String){
        _role.value = role
    }

    fun logout() {
        _userId.value = null
        _username.value = null
        _role.value = null
        _tempSelections.value = emptyMap()
    }
}

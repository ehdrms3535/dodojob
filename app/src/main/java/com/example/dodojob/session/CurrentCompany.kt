package com.example.dodojob.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CurrentCompany {
    private val _name = MutableStateFlow<String?>(null)           // Supabase auth user id (UUID)
    private val _email = MutableStateFlow<String?>(null)     // 로그인 아이디/이메일
    private val _companyid = MutableStateFlow<String>("")           // password
    private val _companylocate = MutableStateFlow<String>("")

    val name: String? get() = _name.value
    val username: String? get() = _email.value
    val companyid: String get() = _companyid.value
    val companylocate: String get() = _companylocate.value

    /** 로그인 시 (username/password) */
    fun setCompanyId(companyid: String) {
        _companyid.value = companyid

    }

    fun setCompanylocate(companylocate : String){
        _companylocate.value = companylocate
    }
}

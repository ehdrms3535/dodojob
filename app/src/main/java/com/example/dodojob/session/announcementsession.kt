package com.example.dodojob.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
// 공고 등록 흐름에서만 쓰는 임시 상태
object AnnouncementSession {

    enum class ApplyMethod { PhoneSms, OnlineForm }

    private val _applyMethod = MutableStateFlow(ApplyMethod.PhoneSms)
    val applyMethodFlow: StateFlow<ApplyMethod> get() = _applyMethod
    private val _sirname = MutableStateFlow("")
    private val _sirphone = MutableStateFlow("")

    val applyMethod: ApplyMethod get() = _applyMethod.value
    val sirname: String get() = _sirname.value
    val sirphone: String get()  = _sirphone.value


    fun setApplyMethod(method: ApplyMethod) {
        _applyMethod.value = method
    }

    fun setsirname(name : String){
        _sirname.value = name
    }

    fun setsirphone(phone : String){
        _sirphone.value = phone
    }


    fun clear() {
        _applyMethod.value = ApplyMethod.PhoneSms
    }
}
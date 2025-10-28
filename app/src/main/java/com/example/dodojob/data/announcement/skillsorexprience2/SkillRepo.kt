package com.example.dodojob.data.announcement.skillsorexprience2

interface SkillRepo{
    suspend fun insertSkill(announcement:SkillDto)
}
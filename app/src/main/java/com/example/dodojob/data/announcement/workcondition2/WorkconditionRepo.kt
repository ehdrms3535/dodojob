package com.example.dodojob.data.announcement.workcondition2

interface WorkconditionRepo{
    suspend fun insertWorkcondition(announcement:WorkConditionDto)
}
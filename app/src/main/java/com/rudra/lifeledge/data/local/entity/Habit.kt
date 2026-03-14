package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HabitCategory { HEALTH, LEARNING, PRODUCTIVITY, MINDFULNESS, FINANCE }
enum class HabitFrequency { DAILY, WEEKLY, CUSTOM }

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val category: HabitCategory,
    val frequency: HabitFrequency,
    val customFrequency: Int?,
    val target: Double,
    val unit: String,
    val icon: String,
    val color: Int,
    val isActive: Boolean,
    val createdAt: Long
)

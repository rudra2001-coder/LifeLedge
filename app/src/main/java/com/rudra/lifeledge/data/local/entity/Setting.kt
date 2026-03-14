package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SettingType { STRING, INT, BOOLEAN, DOUBLE }

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey
    val key: String,
    val value: String,
    val type: SettingType
)

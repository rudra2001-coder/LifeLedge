package com.rudra.lifeledge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Mood { GREAT, GOOD, OKAY, BAD, TERRIBLE }

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val title: String,
    val content: String,
    val mood: Mood,
    val energy: Int,
    val productivity: Int,
    val tags: String,
    val images: String,
    val location: String?,
    val isFavorite: Boolean
)

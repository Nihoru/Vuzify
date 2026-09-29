package com.nihoru.vuzify.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val groupName: String,
    val dateString: String, // e.g. "29.09.2026"
    val timeSlot: String,   // e.g. "11:15 — 12:45"
    val discipline: String,
    val type: String,
    val room: String,
    val teacher: String,
    val isOnline: Boolean,
    val cachedAt: Long,
    val serverUpdatedAt: Long
)

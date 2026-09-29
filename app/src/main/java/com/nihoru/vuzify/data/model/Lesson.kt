package com.nihoru.vuzify.data.model

import androidx.compose.ui.graphics.Color
import com.nihoru.vuzify.ui.theme.ExamBackground
import com.nihoru.vuzify.ui.theme.ExamText
import com.nihoru.vuzify.ui.theme.LabBackground
import com.nihoru.vuzify.ui.theme.LabText
import com.nihoru.vuzify.ui.theme.LectureBackground
import com.nihoru.vuzify.ui.theme.LectureText
import com.nihoru.vuzify.ui.theme.OtherTypeBackground
import com.nihoru.vuzify.ui.theme.OtherTypeText
import com.nihoru.vuzify.ui.theme.PracticeBackground
import com.nihoru.vuzify.ui.theme.PracticeText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class LessonType(val displayName: String, val bg: Color, val text: Color) {
    LAB("ЛАБОРАТОРНАЯ", LabBackground, LabText),
    PRACTICE("ПРАКТИКА", PracticeBackground, PracticeText),
    EXAM("ЗАЧЕТ", ExamBackground, ExamText),
    LECTURE("ЛЕКЦИЯ", LectureBackground, LectureText),
    OTHER("ЗАНЯТИЕ", OtherTypeBackground, OtherTypeText);

    companion object {
        fun fromString(type: String?): LessonType {
            if (type == null) return OTHER
            val lower = type.lowercase()
            return when {
                lower.contains("лабор") -> LAB
                lower.contains("практ") -> PRACTICE
                lower.contains("зачет") || lower.contains("экзамен") -> EXAM
                lower.contains("лекц") -> LECTURE
                else -> OTHER
            }
        }
    }
}

@Serializable
data class LessonDto(
    @SerialName("ID") val id: String? = null,
    @SerialName("UF_DISCIPLINE") val discipline: String? = null,
    @SerialName("UF_TIME_START") val timeStart: String? = null,
    @SerialName("UF_TIME_END") val timeEnd: String? = null,
    @SerialName("UF_TYPE") val type: String? = null,
    @SerialName("UF_AUDIENCE") val audiences: List<String> = emptyList(),
    @SerialName("UF_TEACHER") val teachers: List<String> = emptyList(),
    @SerialName("UF_GROUP") val groups: List<String> = emptyList(),
    @SerialName("UF_IS_ONLINE") val isOnline: String? = null,
    @SerialName("UF_IS_RETAKE") val isRetake: String? = null
)

@Serializable
data class ScheduleApiResponse(
    val status: String,
    val data: ScheduleApiData? = null
)

@Serializable
data class ScheduleApiData(
    @SerialName("updated_at") val updatedAt: Long = 0,
    @SerialName("week_dates") val weekDates: List<String> = emptyList(),
    val days: Map<String, List<LessonDto>> = emptyMap()
)

@Serializable
data class SearchGroupDto(
    @SerialName("UF_NAME") val name: String,
    val type: String,
    val value: String
)

@Serializable
data class SearchApiResponse(
    val status: String,
    val data: List<SearchGroupDto> = emptyList()
)

data class Lesson(
    val id: String,
    val timeSlot: String,
    val title: String,
    val type: LessonType,
    val room: String,
    val teacher: String,
    val isOnline: Boolean
)

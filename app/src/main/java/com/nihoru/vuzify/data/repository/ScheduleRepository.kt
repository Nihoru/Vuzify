package com.nihoru.vuzify.data.repository

import com.nihoru.vuzify.data.local.LessonDao
import com.nihoru.vuzify.data.local.LessonEntity
import com.nihoru.vuzify.data.model.Lesson
import com.nihoru.vuzify.data.model.LessonType
import com.nihoru.vuzify.data.remote.MtuciApiClient
import com.nihoru.vuzify.data.remote.MtuciSessionBridge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleRepository(
    private val lessonDao: LessonDao,
    private val apiClient: MtuciApiClient,
    private val sessionBridge: MtuciSessionBridge
) {

    fun getLessonsForDay(group: String, dateString: String): Flow<List<Lesson>> {
        return lessonDao.getLessonsForDay(group, dateString).map { entities ->
            entities.map { entity ->
                Lesson(
                    id = entity.localId.toString(),
                    timeSlot = entity.timeSlot,
                    title = entity.discipline,
                    type = LessonType.fromString(entity.type),
                    room = entity.room.ifEmpty { "—" },
                    teacher = entity.teacher.ifEmpty { "—" },
                    isOnline = entity.isOnline
                )
            }
        }
    }

    suspend fun getLastSyncInfo(group: String): String? {
        val serverUpdated = lessonDao.getServerUpdatedAt(group)
        val localCached = lessonDao.getLastCacheTimestamp(group)

        val timestamp = when {
            serverUpdated != null && serverUpdated > 0 -> serverUpdated * 1000
            localCached != null && localCached > 0 -> localCached
            else -> return null
        }

        val sdf = SimpleDateFormat("d MMMM в HH:mm", Locale("ru"))
        return sdf.format(Date(timestamp))
    }

    suspend fun refreshSchedule(group: String, month: Int): Result<Unit> {
        return try {
            val sessionResult = sessionBridge.obtainSession()
            val session = sessionResult.getOrThrow()

            val response = apiClient.getSchedule(group, month, session)
            val scheduleData = response.data ?: return Result.failure(Exception("Пустые данные расписания"))

            val currentTimestamp = System.currentTimeMillis()
            val serverUpdated = scheduleData.updatedAt

            val entitiesToInsert = mutableListOf<LessonEntity>()

            scheduleData.days.forEach { (dateStr, lessonDtos) ->
                lessonDtos.forEach { dto ->
                    val timeStart = dto.timeStart ?: ""
                    val timeEnd = dto.timeEnd ?: ""
                    val timeSlot = if (timeStart.isNotEmpty() && timeEnd.isNotEmpty()) {
                        "$timeStart — $timeEnd"
                    } else timeStart

                    val room = dto.audiences.firstOrNull() ?: ""
                    val teacher = dto.teachers.firstOrNull() ?: ""

                    entitiesToInsert.add(
                        LessonEntity(
                            groupName = group,
                            dateString = dateStr,
                            timeSlot = timeSlot,
                            discipline = dto.discipline ?: "Без названия",
                            type = dto.type ?: "Занятие",
                            room = room,
                            teacher = teacher,
                            isOnline = dto.isOnline == "1",
                            cachedAt = currentTimestamp,
                            serverUpdatedAt = serverUpdated
                        )
                    )
                }
            }

            if (entitiesToInsert.isNotEmpty()) {
                lessonDao.insertAll(entitiesToInsert)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

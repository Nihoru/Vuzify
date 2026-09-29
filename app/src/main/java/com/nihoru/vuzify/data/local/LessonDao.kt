package com.nihoru.vuzify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons WHERE groupName = :group AND dateString = :date ORDER BY localId ASC")
    fun getLessonsForDay(group: String, date: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE groupName = :group ORDER BY localId ASC")
    fun getAllLessonsForGroup(group: String): Flow<List<LessonEntity>>

    @Query("SELECT MAX(cachedAt) FROM lessons WHERE groupName = :group")
    suspend fun getLastCacheTimestamp(group: String): Long?

    @Query("SELECT MAX(serverUpdatedAt) FROM lessons WHERE groupName = :group")
    suspend fun getServerUpdatedAt(group: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons WHERE groupName = :group")
    suspend fun clearGroupSchedule(group: String)
}

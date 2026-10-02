package com.fyrefly.fireflycollege.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fyrefly.fireflycollege.data.database.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    fun observeById(id: Long): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getById(id: Long): CourseEntity?

    @Query("SELECT * FROM courses ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<CourseEntity>

    @Insert
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Long)
}

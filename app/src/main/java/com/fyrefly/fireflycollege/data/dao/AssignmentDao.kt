package com.fyrefly.fireflycollege.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fyrefly.fireflycollege.data.database.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {

    @Query("SELECT * FROM assignments ORDER BY isCompleted ASC, dueAt ASC")
    fun observeAll(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :id")
    fun observeById(id: Long): Flow<AssignmentEntity?>

    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY isCompleted ASC, dueAt ASC")
    fun observeForCourse(courseId: Long): Flow<List<AssignmentEntity>>

    /** Ready for the calendar stage: assignments whose dueAt falls in [startOfDay, endOfDay]. */
    @Query("SELECT * FROM assignments WHERE dueAt BETWEEN :startOfDay AND :endOfDay ORDER BY dueAt ASC")
    fun observeDueBetween(startOfDay: Long, endOfDay: Long): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getById(id: Long): AssignmentEntity?

    @Query("SELECT * FROM assignments ORDER BY dueAt ASC")
    suspend fun getAll(): List<AssignmentEntity>

    @Insert
    suspend fun insertAll(assignments: List<AssignmentEntity>)

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(assignment: AssignmentEntity): Long

    @Update
    suspend fun update(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteById(id: Long)
}

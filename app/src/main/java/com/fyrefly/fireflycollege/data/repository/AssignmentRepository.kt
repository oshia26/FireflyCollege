package com.fyrefly.fireflycollege.data.repository

import com.fyrefly.fireflycollege.data.dao.AssignmentDao
import com.fyrefly.fireflycollege.data.database.entity.toDomain
import com.fyrefly.fireflycollege.data.database.entity.toEntity
import com.fyrefly.fireflycollege.data.model.Assignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AssignmentRepository(private val dao: AssignmentDao) {

    fun observeAll(): Flow<List<Assignment>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeById(id: Long): Flow<Assignment?> =
        dao.observeById(id).map { it?.toDomain() }

    fun observeForCourse(courseId: Long): Flow<List<Assignment>> =
        dao.observeForCourse(courseId).map { list -> list.map { it.toDomain() } }

    fun observeDueBetween(startOfDay: Long, endOfDay: Long): Flow<List<Assignment>> =
        dao.observeDueBetween(startOfDay, endOfDay).map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): Assignment? = dao.getById(id)?.toDomain()

    /** Insert when id == 0, update otherwise. Returns the row id. */
    suspend fun save(assignment: Assignment): Long =
        if (assignment.id == 0L) dao.insert(assignment.toEntity())
        else {
            dao.update(assignment.toEntity())
            assignment.id
        }

    suspend fun delete(assignmentId: Long) = dao.deleteById(assignmentId)
}

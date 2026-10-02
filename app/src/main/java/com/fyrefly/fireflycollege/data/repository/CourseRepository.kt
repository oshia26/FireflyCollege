package com.fyrefly.fireflycollege.data.repository

import com.fyrefly.fireflycollege.data.dao.CourseDao
import com.fyrefly.fireflycollege.data.database.entity.toDomain
import com.fyrefly.fireflycollege.data.database.entity.toEntity
import com.fyrefly.fireflycollege.data.model.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepository(private val dao: CourseDao) {

    fun observeAll(): Flow<List<Course>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeById(id: Long): Flow<Course?> =
        dao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: Long): Course? = dao.getById(id)?.toDomain()

    suspend fun getAll(): List<Course> = dao.getAll().map { it.toDomain() }

    suspend fun create(course: Course): Long = dao.insert(course.toEntity())

    suspend fun update(course: Course) = dao.update(course.toEntity())

    suspend fun delete(courseId: Long) = dao.deleteById(courseId)
}

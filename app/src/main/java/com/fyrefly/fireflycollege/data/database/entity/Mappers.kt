package com.fyrefly.fireflycollege.data.database.entity

import com.fyrefly.fireflycollege.data.model.Assignment
import com.fyrefly.fireflycollege.data.model.Course
import com.fyrefly.fireflycollege.data.model.Priority

fun CourseEntity.toDomain(): Course = Course(
    id = id,
    name = name,
    lecturer = lecturer,
    room = room,
    colorArgb = colorArgb,
    createdAt = createdAt
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id,
    name = name,
    lecturer = lecturer,
    room = room,
    colorArgb = colorArgb,
    createdAt = createdAt
)

fun AssignmentEntity.toDomain(): Assignment = Assignment(
    id = id,
    title = title,
    description = description,
    courseId = courseId,
    createdAt = createdAt,
    dueAt = dueAt,
    priority = runCatching { Priority.valueOf(priority) }.getOrDefault(Priority.MEDIUM),
    isCompleted = isCompleted,
    completedAt = completedAt,
    notes = notes
)

fun Assignment.toEntity(): AssignmentEntity = AssignmentEntity(
    id = id,
    title = title,
    description = description,
    courseId = courseId,
    createdAt = createdAt,
    dueAt = dueAt,
    priority = priority.name,
    isCompleted = isCompleted,
    completedAt = completedAt,
    notes = notes
)

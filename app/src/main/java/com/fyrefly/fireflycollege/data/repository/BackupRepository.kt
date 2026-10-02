package com.fyrefly.fireflycollege.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.fyrefly.fireflycollege.data.database.FireflyDatabase
import com.fyrefly.fireflycollege.data.database.entity.toDomain
import com.fyrefly.fireflycollege.data.database.entity.toEntity
import com.fyrefly.fireflycollege.data.model.BackupFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Backup outcome for UI messaging. */
sealed interface BackupResult {
    data class Exported(val courses: Int, val assignments: Int) : BackupResult
    data class Imported(val courses: Int, val assignments: Int) : BackupResult
    data class Failure(val message: String) : BackupResult
}

/**
 * Export/import of the whole local database as portable JSON through SAF uris,
 * so no storage permissions are ever needed. Import fully replaces current data.
 */
class BackupRepository(
    private val context: Context,
    private val database: FireflyDatabase
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportTo(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val payload = BackupFile(
                exportedAt = System.currentTimeMillis(),
                courses = database.courseDao().getAll().map { it.toDomain() },
                assignments = database.assignmentDao().getAll().map { it.toDomain() }
            )
            val bytes = json.encodeToString(BackupFile.serializer(), payload).encodeToByteArray()
            val stream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext BackupResult.Failure("Could not open the chosen location")
            stream.use { it.write(bytes) }
            BackupResult.Exported(payload.courses.size, payload.assignments.size)
        } catch (e: Exception) {
            BackupResult.Failure(e.message ?: "Export failed")
        }
    }

    /** Full replace semantics — exactly right for recovery after reinstalling the app. */
    suspend fun importFrom(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().decodeToString()
            } ?: return@withContext BackupResult.Failure("Could not read the chosen file")

            val payload = json.decodeFromString(BackupFile.serializer(), text)
            if (payload.format != "fireflycollege-backup") {
                return@withContext BackupResult.Failure("Not a FireflyCollege backup file")
            }

            database.withTransaction {
                database.assignmentDao().deleteAll()
                database.courseDao().deleteAll()
                database.courseDao().insertAll(payload.courses.map { it.toEntity() })
                database.assignmentDao().insertAll(payload.assignments.map { it.toEntity() })
            }
            BackupResult.Imported(payload.courses.size, payload.assignments.size)
        } catch (e: Exception) {
            BackupResult.Failure(e.message ?: "Invalid backup file")
        }
    }
}

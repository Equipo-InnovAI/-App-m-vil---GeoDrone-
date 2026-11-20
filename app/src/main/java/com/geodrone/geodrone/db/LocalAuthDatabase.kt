package com.geodrone.geodrone.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.geodrone.geodrone.model.Project
import java.util.UUID

class LocalAuthDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "geodrone_local_db.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_USERS = "local_users"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PASSWORD = "password"

        private const val TABLE_PROJECTS = "local_projects"
        private const val COLUMN_PROJ_ID = "id"
        private const val COLUMN_PROJ_NAME = "name"
        private const val COLUMN_PROJ_DESC = "description"
        private const val COLUMN_PROJ_LOC = "location"
        private const val COLUMN_PROJ_STATUS = "status"
        private const val COLUMN_PROJ_MEDIA_COUNT = "media_count"

        private const val TABLE_MEDIA = "local_media"
        private const val COLUMN_MEDIA_ID = "id"
        private const val COLUMN_MEDIA_PROJ_ID = "project_id"
        private const val COLUMN_MEDIA_URI = "uri"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_EMAIL TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_PASSWORD TEXT
            )
        """.trimIndent()

        val createProjectsTable = """
            CREATE TABLE $TABLE_PROJECTS (
                $COLUMN_PROJ_ID TEXT PRIMARY KEY,
                $COLUMN_PROJ_NAME TEXT,
                $COLUMN_PROJ_DESC TEXT,
                $COLUMN_PROJ_LOC TEXT,
                $COLUMN_PROJ_STATUS TEXT,
                $COLUMN_PROJ_MEDIA_COUNT INTEGER
            )
        """.trimIndent()

        val createMediaTable = """
            CREATE TABLE $TABLE_MEDIA (
                $COLUMN_MEDIA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MEDIA_PROJ_ID TEXT,
                $COLUMN_MEDIA_URI TEXT
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createProjectsTable)
        db.execSQL(createMediaTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEDIA")
        onCreate(db)
    }

    enum class RegisterResult {
        SUCCESS,
        USER_ALREADY_EXISTS,
        ERROR
    }

    enum class LoginResult {
        SUCCESS,
        USER_NOT_FOUND,
        WRONG_PASSWORD,
        ERROR
    }

    fun registerUser(name: String, email: String, pass: String): RegisterResult {
        val dbRead = readableDatabase
        val cursor = dbRead.query(
            TABLE_USERS,
            arrayOf(COLUMN_EMAIL),
            "$COLUMN_EMAIL = ?",
            arrayOf(email.lowercase()),
            null, null, null
        )
        val exists = cursor.use { it.moveToFirst() }
        if (exists) {
            return RegisterResult.USER_ALREADY_EXISTS
        }

        val dbWrite = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email.lowercase())
            put(COLUMN_NAME, name)
            put(COLUMN_PASSWORD, pass)
        }
        val result = dbWrite.insert(TABLE_USERS, null, values)
        return if (result != -1L) RegisterResult.SUCCESS else RegisterResult.ERROR
    }

    fun loginUser(email: String, pass: String): LoginResult {
        val dbRead = readableDatabase
        val cursor = dbRead.query(
            TABLE_USERS,
            arrayOf(COLUMN_EMAIL, COLUMN_PASSWORD),
            "$COLUMN_EMAIL = ?",
            arrayOf(email.lowercase()),
            null, null, null
        )

        return cursor.use {
            if (!it.moveToFirst()) {
                LoginResult.USER_NOT_FOUND
            } else {
                val storedPass = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                if (storedPass == pass) {
                    LoginResult.SUCCESS
                } else {
                    LoginResult.WRONG_PASSWORD
                }
            }
        }
    }

    fun saveOrUpdateProject(
        id: String?,
        name: String,
        description: String,
        location: String,
        status: String,
        mediaUris: List<String>
    ): String {
        val projId = id.takeUnless { it.isNullOrBlank() } ?: UUID.randomUUID().toString()
        val dbWrite = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_PROJ_ID, projId)
            put(COLUMN_PROJ_NAME, name)
            put(COLUMN_PROJ_DESC, description)
            put(COLUMN_PROJ_LOC, location)
            put(COLUMN_PROJ_STATUS, status)
            put(COLUMN_PROJ_MEDIA_COUNT, mediaUris.size)
        }

        dbWrite.insertWithOnConflict(
            TABLE_PROJECTS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        for (uri in mediaUris) {
            val mediaValues = ContentValues().apply {
                put(COLUMN_MEDIA_PROJ_ID, projId)
                put(COLUMN_MEDIA_URI, uri)
            }
            dbWrite.insert(TABLE_MEDIA, null, mediaValues)
        }

        return projId
    }

    fun getAllProjects(): List<Project> {
        val dbRead = readableDatabase
        val cursor = dbRead.query(
            TABLE_PROJECTS,
            null, null, null, null, null, null
        )
        val list = mutableListOf<Project>()
        cursor.use {
            while (it.moveToNext()) {
                val proj = Project(
                    id = it.getString(it.getColumnIndexOrThrow(COLUMN_PROJ_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_PROJ_NAME)),
                    description = it.getString(it.getColumnIndexOrThrow(COLUMN_PROJ_DESC)).orEmpty(),
                    location = it.getString(it.getColumnIndexOrThrow(COLUMN_PROJ_LOC)).orEmpty(),
                    status = it.getString(it.getColumnIndexOrThrow(COLUMN_PROJ_STATUS)).orEmpty()
                )
                list.add(proj)
            }
        }
        return list
    }

    fun getProjectMediaCount(projId: String): Int {
        val dbRead = readableDatabase
        val cursor = dbRead.query(
            TABLE_MEDIA,
            arrayOf("COUNT(*)"),
            "$COLUMN_MEDIA_PROJ_ID = ?",
            arrayOf(projId),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }
}

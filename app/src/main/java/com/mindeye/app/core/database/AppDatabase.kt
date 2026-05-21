package com.mindeye.app.core.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindeye.app.core.database.dao.*
import com.mindeye.app.core.database.entity.*

/**
 * 应用数据库
 * 使用 Room 持久化库
 */
@Database(
    entities = [
        UserEntity::class,
        SceneRecordEntity::class,
        ChatHistoryEntity::class,
        PostCacheEntity::class,
        EmergencyContactEntity::class,
        SettingsEntity::class,
        CommentEntity::class,
        HelpRequestEntity::class,
        AiQaEntity::class,
        EncouragementEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sceneRecordDao(): SceneRecordDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun postCacheDao(): PostCacheDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun settingsDao(): SettingsDao
    abstract fun commentDao(): CommentDao
    abstract fun helpRequestDao(): HelpRequestDao
    abstract fun aiQaDao(): AiQaDao
    abstract fun encouragementDao(): EncouragementDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE post_cache ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS help_requests (
                        requestId TEXT PRIMARY KEY NOT NULL,
                        requesterId TEXT NOT NULL,
                        requesterName TEXT NOT NULL,
                        content TEXT NOT NULL,
                        location TEXT,
                        latitude REAL,
                        longitude REAL,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        volunteerId TEXT,
                        volunteerName TEXT,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_qa_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        question TEXT NOT NULL,
                        answer TEXT NOT NULL,
                        timestamp INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS encouragements (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        triggerScene TEXT NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS comments (
                        commentId TEXT PRIMARY KEY NOT NULL,
                        postId TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        userName TEXT NOT NULL,
                        content TEXT NOT NULL,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS settings (
                        key TEXT PRIMARY KEY NOT NULL,
                        value TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        private fun buildDatabase(context: android.content.Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mindeye_database"
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}

/**
 * 类型转换器
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString("|")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split("|")
    }

    @TypeConverter
    fun fromFloatArray(value: FloatArray): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toFloatArray(value: String): FloatArray {
        return if (value.isEmpty()) floatArrayOf() else value.split(",").map { it.toFloat() }.toFloatArray()
    }

    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return value.joinToString("|")
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return if (value.isEmpty()) emptyList() else value.split("|").map { it.toLong() }
    }
}

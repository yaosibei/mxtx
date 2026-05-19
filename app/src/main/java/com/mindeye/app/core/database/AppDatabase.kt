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
        TravelRecordEntity::class,
        VolunteerOrderEntity::class,
        MoodRecordEntity::class,
        HelpHistoryEntity::class,
        UserBadgeEntity::class,
        VolunteerProfileEntity::class,
        HelpRequestSyncEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sceneRecordDao(): SceneRecordDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun postCacheDao(): PostCacheDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun settingsDao(): SettingsDao
    abstract fun travelRecordDao(): TravelRecordDao
    abstract fun volunteerOrderDao(): VolunteerOrderDao
    abstract fun moodRecordDao(): MoodRecordDao
    abstract fun helpHistoryDao(): HelpHistoryDao
    abstract fun userBadgeDao(): UserBadgeDao
    abstract fun volunteerProfileDao(): VolunteerProfileDao
    abstract fun helpRequestSyncDao(): HelpRequestSyncDao

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
                db.execSQL("ALTER TABLE post_cache ADD COLUMN postType TEXT NOT NULL DEFAULT 'TEXT'")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN status TEXT NOT NULL DEFAULT 'OPEN'")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN locationName TEXT")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN volunteerName TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'user'")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS travel_records (" +
                        "recordId TEXT NOT NULL PRIMARY KEY, " +
                        "userId TEXT NOT NULL, " +
                        "startTime INTEGER NOT NULL, " +
                        "endTime INTEGER, " +
                        "startLocation TEXT, " +
                        "endLocation TEXT, " +
                        "startLatitude REAL, " +
                        "startLongitude REAL, " +
                        "endLatitude REAL, " +
                        "endLongitude REAL, " +
                        "distance REAL, " +
                        "duration INTEGER, " +
                        "sceneRecords INTEGER NOT NULL DEFAULT 0, " +
                        "isCompleted INTEGER NOT NULL DEFAULT 0, " +
                        "createdAt INTEGER NOT NULL DEFAULT 0)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS volunteer_orders (" +
                        "orderId TEXT NOT NULL PRIMARY KEY, " +
                        "postId TEXT NOT NULL, " +
                        "requesterId TEXT NOT NULL, " +
                        "requesterName TEXT NOT NULL, " +
                        "volunteerId TEXT NOT NULL, " +
                        "volunteerName TEXT NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'pending', " +
                        "content TEXT NOT NULL, " +
                        "latitude REAL, " +
                        "longitude REAL, " +
                        "createdAt INTEGER NOT NULL DEFAULT 0, " +
                        "acceptedAt INTEGER, " +
                        "completedAt INTEGER)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE post_cache ADD COLUMN helpCategory TEXT NOT NULL DEFAULT 'OTHER'")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN urgencyLevel TEXT NOT NULL DEFAULT 'LOW'")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN comments TEXT NOT NULL DEFAULT '[]'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS mood_records (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "userId TEXT NOT NULL, " +
                        "mood TEXT NOT NULL, " +
                        "moodLabel TEXT NOT NULL, " +
                        "timestamp INTEGER NOT NULL DEFAULT 0, " +
                        "note TEXT)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS help_history (" +
                        "historyId TEXT NOT NULL PRIMARY KEY, " +
                        "userId TEXT NOT NULL, " +
                        "postId TEXT NOT NULL, " +
                        "helperId TEXT NOT NULL, " +
                        "helperName TEXT NOT NULL, " +
                        "helpCategory TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "rating INTEGER NOT NULL DEFAULT 0, " +
                        "feedback TEXT, " +
                        "createdAt INTEGER NOT NULL DEFAULT 0, " +
                        "completedAt INTEGER)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS user_badges (" +
                        "badgeId TEXT NOT NULL PRIMARY KEY, " +
                        "userId TEXT NOT NULL, " +
                        "badgeKey TEXT NOT NULL, " +
                        "badgeName TEXT NOT NULL, " +
                        "badgeIcon TEXT NOT NULL, " +
                        "badgeDescription TEXT NOT NULL, " +
                        "unlockedAt INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE post_cache ADD COLUMN volunteerId TEXT")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN rating INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN feedback TEXT")
                db.execSQL("ALTER TABLE post_cache ADD COLUMN completedAt INTEGER")
                db.execSQL("ALTER TABLE volunteer_orders ADD COLUMN helpCategory TEXT NOT NULL DEFAULT 'OTHER'")
                db.execSQL("ALTER TABLE volunteer_orders ADD COLUMN rating INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE volunteer_orders ADD COLUMN feedback TEXT")
                db.execSQL("ALTER TABLE volunteer_orders ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS volunteer_profiles (" +
                        "volunteerId TEXT NOT NULL PRIMARY KEY, " +
                        "userId TEXT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "avatar TEXT, " +
                        "serviceCount INTEGER NOT NULL DEFAULT 0, " +
                        "completedOrders INTEGER NOT NULL DEFAULT 0, " +
                        "rating REAL NOT NULL DEFAULT 5.0, " +
                        "totalHours INTEGER NOT NULL DEFAULT 0, " +
                        "joinDate INTEGER NOT NULL DEFAULT 0, " +
                        "level TEXT NOT NULL DEFAULT 'NEWBIE', " +
                        "isActive INTEGER NOT NULL DEFAULT 1, " +
                        "updatedAt INTEGER NOT NULL DEFAULT 0)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS help_request_sync (" +
                        "syncId TEXT NOT NULL PRIMARY KEY, " +
                        "postId TEXT NOT NULL, " +
                        "operation TEXT NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'pending', " +
                        "retryCount INTEGER NOT NULL DEFAULT 0, " +
                        "createdAt INTEGER NOT NULL DEFAULT 0, " +
                        "syncedAt INTEGER)"
                )
            }
        }

        private fun buildDatabase(context: android.content.Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mindeye_database"
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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

package com.example.disipline.data.Local

//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//
//@Database(
//    entities = [HabitEntity::class, SessionEntity::class, SectionEntity::class],
//    version = 1,
//    exportSchema = false
//)
//abstract class HabitDatabase : RoomDatabase() {
//
//    abstract fun habitDao(): HabitDao
//
//    companion object {
//        @Volatile
//        private var instance: HabitDatabase? = null
//
//        fun get(context: Context): HabitDatabase =
//            instance ?: synchronized(this) {
//                instance ?: Room.databaseBuilder(
//                    context.applicationContext,
//                    HabitDatabase::class.java,
//                    "disciplinex.db"
//                ).build().also { instance = it }
//            }
//    }
//}




import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.disipline.data.Local.profile.BlockedAppEntity
import com.example.disipline.data.Local.profile.ProfileEntity

@Database(
    entities = [
        HabitEntity::class,
        SessionEntity::class,
        SectionEntity::class,
        ProfileEntity::class,        // NEW
        BlockedAppEntity::class      // NEW
    ],
    version = 2,                     // bumped from 1 -> 2
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun profileDao(): ProfileDao      // NEW

    companion object {
        @Volatile
        private var instance: HabitDatabase? = null

        // Adds the two new tables without touching existing habits,
        // sessions, or sections. Safe to run on an existing install.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `quote` TEXT NOT NULL,
                        `avatarPath` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `blocked_apps` (
                        `packageName` TEXT NOT NULL,
                        `appName` TEXT NOT NULL,
                        PRIMARY KEY(`packageName`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun get(context: Context): HabitDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "disciplinex.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
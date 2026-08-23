package com.chezley.onepiecetcg.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The app's Room database. UI/repository code should go through
 * [getInstance] rather than constructing a [RoomDatabase.Builder] directly,
 * so the schema and singleton lifecycle stay in one place.
 */
@Database(
    entities = [CardEntity::class, CardSetEntity::class, OwnedCardEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun cardSetDao(): CardSetDao
    abstract fun ownedCardDao(): OwnedCardDao

    companion object {
        private const val DATABASE_NAME = "onepiecetcg.db"

        @Volatile
        private var instance: AppDatabase? = null

        /** File-backed singleton, so data persists across app relaunch. */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context, DATABASE_NAME).also { instance = it }
            }

        private fun build(context: Context, name: String): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name).build()
    }
}

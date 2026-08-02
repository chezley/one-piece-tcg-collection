package com.chezley.onepiecetcg.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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
        private const val DATABASE_NAME = "one_piece_tcg.db"

        @Volatile
        private var instance: AppDatabase? = null

        /** Returns the app-wide singleton [AppDatabase], creating it on first access. */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
    }
}

package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        QuranVerseEntity::class,
        HadithEntity::class,
        DhikrEntity::class,
        FavoriteEntity::class,
        ContentSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AtharDatabase : RoomDatabase() {

    abstract fun quranDao(): QuranDao
    abstract fun hadithDao(): HadithDao
    abstract fun dhikrDao(): DhikrDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun contentSettingsDao(): ContentSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AtharDatabase? = null

        fun getDatabase(context: Context): AtharDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AtharDatabase::class.java,
                    "athar_islamic_content.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial verified package asynchronously upon initial creation
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            seedInitialVerifiedData(database)
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Ensure data is seeded even if initial creation callback was interrupted
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            if (database.quranDao().getTotalCount() == 0) {
                                seedInitialVerifiedData(database)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialVerifiedData(database: AtharDatabase) {
            database.quranDao().insertVerses(InitialSeedData.quranVerses)
            database.hadithDao().insertHadiths(InitialSeedData.hadiths)
            database.dhikrDao().insertDhikr(InitialSeedData.adhkar)
            database.contentSettingsDao().insertInitialSettings(InitialSeedData.contentSettings)
        }
    }
}

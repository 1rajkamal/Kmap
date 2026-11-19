package com.breadcrumbradar.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.breadcrumbradar.app.data.model.BreadcrumbPoint
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.data.model.TrailSession

@Database(
    entities = [SavedLocation::class, BreadcrumbPoint::class, TrailSession::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun breadcrumbDao(): BreadcrumbDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "breadcrumb_radar_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

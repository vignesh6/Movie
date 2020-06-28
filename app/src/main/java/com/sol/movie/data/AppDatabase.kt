package com.sol.movie.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sol.movie.module.detail.data.MovieDetail
import com.sol.movie.module.detail.data.MovieDetailDao
import com.sol.movie.module.search.data.MovieDao
import com.sol.movie.module.search.data.Movie

/**
 * The Room database for this app
 */
@Database(
    entities = [Movie::class,MovieDetail::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

   abstract fun movieDao(): MovieDao
   abstract fun detaiDao(): MovieDetailDao
    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "movie-db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                })
                .build()
        }
    }
}

package com.sol.movie.ui.data

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies:List<Movie>)

    @Query("SELECT * FROM movie WHERE Title = :tittle ORDER BY year DESC")
    fun getPagedMovieByTittle(tittle: String): DataSource.Factory<Int, Movie>

    @Query("SELECT * FROM movie ORDER BY year DESC")
    fun getPagedMovies(): DataSource.Factory<Int, Movie>
}
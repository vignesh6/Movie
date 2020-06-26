package com.sol.movie.ui.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies:List<Movie>)

    @Query("SELECT * FROM movie WHERE Title like :tittle")
    fun getPagedMovieByTittle(tittle: String): PagingSource<Int, Movie>

    @Query("DELETE FROM movie")
    suspend fun clearMovies()

}
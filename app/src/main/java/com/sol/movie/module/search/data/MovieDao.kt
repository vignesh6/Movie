package com.sol.movie.module.search.data

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(movies:List<Movie>)

    @Query("DELETE FROM movie WHERE searchQuery = :searchString ")
    fun clearMovies(searchString: String)

    @Query("SELECT MAX(indexInResponse) + 1 FROM movie WHERE searchQuery = :query")
    fun getNextIndexInMovie(query: String):Int

    @Query("SELECT * FROM movie WHERE searchQuery = :searchString ORDER BY indexInResponse ASC")
    fun moviesBySearchQuery(searchString: String) : DataSource.Factory<Int, Movie>
}
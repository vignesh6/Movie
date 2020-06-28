package com.sol.movie.module.detail.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(movieDetail: MovieDetail)

    @Query("SELECT * FROM detail WHERE Title =:title")
    fun getMovieDetail(title:String):LiveData<MovieDetail>

}
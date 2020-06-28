package com.sol.movie.api


import androidx.lifecycle.LiveData
import com.sol.movie.data.repo.ApiResponse
import com.sol.movie.module.detail.data.MovieDetail
import com.sol.movie.module.search.data.MovieResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    companion object {
        const val API_END_POINT_URL = "http://www.omdbapi.com/"
        const val API_KEY = "5d81e1ce"
        const val TYPE = "movie"
    }

    @GET("/")
    fun getDetailByTittle(
        @Query("plot") plot: String,
        @Query("apikey") apiKey: String,
        @Query("t") title: String
    ): LiveData<ApiResponse<MovieDetail>>

    @GET("/")
    fun searchMoviesCall(
        @Query("type") type: String,
        @Query("apikey") apiKey: String,
        @Query("page") page: String,
        @Query("s") searchString: String
    ): Call<MovieResult>
}
package com.sol.movie.api


import com.sol.movie.ui.data.MovieResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    companion object{
        const val API_END_POINT_URL = "http://www.omdbapi.com/"
        const val API_KEY = "5d81e1ce"
        const val TYPE = "movie"
    }
    @GET("/")
    suspend fun searchMovies(@Query("type")type:String,@Query("apikey")apikey:String,@Query("page")page:String,@Query("s")searchString:String): MovieResult
}
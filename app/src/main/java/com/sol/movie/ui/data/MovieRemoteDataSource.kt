package com.sol.movie.ui.data

import com.sol.movie.api.ApiInterface
import com.sol.movie.api.BaseDataSource
import javax.inject.Inject

class MovieRemoteDataSource @Inject constructor(private val service: ApiInterface):BaseDataSource() {
    suspend fun searchMovies(searchQuery:String,pageNo:String) = getResult {
        service.searchMovies(ApiInterface.TYPE,ApiInterface.API_KEY,pageNo,searchQuery)
    }
}
package com.sol.movie.ui.data

import com.sol.movie.api.ApiInterface
import com.sol.movie.api.BaseDataSource
import javax.inject.Inject

class MovieRemoteDataSource @Inject constructor(private val service: ApiInterface):BaseDataSource() {

}
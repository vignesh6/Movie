package com.sol.movie.data.repo

import androidx.lifecycle.LiveData
import com.sol.movie.api.ApiInterface
import com.sol.movie.data.AppDatabase
import com.sol.movie.module.detail.data.MovieDetail
import com.sol.movie.util.AppExecutors
import javax.inject.Inject
import com.sol.movie.data.repo.MovieBoundaryCallback
import timber.log.Timber

class DetailRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val apiInterface: ApiInterface,
    private val database: AppDatabase
) {
    fun getMovieDetail(title: String): LiveData<Resource<MovieDetail>> {
        return object : NetworkBoundResource<MovieDetail, MovieDetail>(appExecutors) {
            override fun saveCallResult(item: MovieDetail) {
                Timber.e("insert ${item.toString()}")
                database.detaiDao().insert(item)
            }

            override fun shouldFetch(data: MovieDetail?) = data == null

            override fun loadFromDb() = database.detaiDao().getMovieDetail(title)

            override fun createCall() =
                apiInterface.getDetailByTittle(plot = "short",title =  title,apiKey =  ApiInterface.API_KEY)
        }.asLiveData()
    }
}
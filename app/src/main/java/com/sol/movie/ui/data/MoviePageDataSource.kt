package com.sol.movie.ui.data

import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.sol.movie.data.Result

class MoviePageDataSource @Inject constructor(private val query:String,private val movieRemoteDataSource: MovieRemoteDataSource,private val movieDao: MovieDao,private val scope: CoroutineScope):
    PageKeyedDataSource<Int, Movie>() {
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Movie>
    ) {
        fetchData(1, params.requestedLoadSize) {
            callback.onResult(it, null, 2)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Movie>) {
        val page = params.key
        fetchData(page, params.requestedLoadSize) {
            callback.onResult(it, page + 1)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Movie>) {
        TODO("Not yet implemented")
    }

    private fun fetchData(page: Int, pageSize: Int, callback: (List<Movie>) -> Unit) {
        scope.launch(getJobErrorHandler()) {
        /*    val response = movieRemoteDataSource.searchMovies(query,page.toString())
            if (response.status == Result.Status.SUCCESS) {
                val results = response.data!!.Search
                movieDao.insertAll(results)

                callback(results)
            } else if (response.status == Result.Status.ERROR) {
                postError(response.message!!)
            }*/
        }
    }
    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        postError(e.message ?: e.toString())
    }

    private fun postError(message: String) {
        Timber.e("An error happened: $message")
        // TODO network error handling
        //networkState.postValue(NetworkState.FAILED)
    }
}
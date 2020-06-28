/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sol.movie.data.repo

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.toLiveData
import com.sol.movie.api.ApiInterface
import com.sol.movie.data.AppDatabase
import com.sol.movie.data.NetworkState
import com.sol.movie.module.search.data.Movie
import com.sol.movie.module.search.data.MovieResult
import com.sol.movie.util.AppExecutors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 */
const val NO_DATA_FOUND: String = "No Data Found!"
class MoviesDBRepository @Inject constructor(
    val db: AppDatabase,
    private val api: ApiInterface,
    private val ioExecutor: AppExecutors
) {

    /**
     * Inserts the response into the database while also assigning position indices,search query to items.
     */
    private fun insertResultIntoDb(query: String, result: MovieResult?) {
        if (result != null && result.Response.equals("True", ignoreCase = true)) {
            result.Search.let { posts ->
                db.runInTransaction {
                    val start = db.movieDao().getNextIndexInMovie(query)
                    val page = start / 10
                    Timber.e("$page $start")
                    val items = posts.mapIndexed { index, child ->
                        child.indexInResponse = start + index
                        child.searchQuery = query
                        child.page = page
                        child
                    }
                    db.movieDao().insertAll(items)
                }
            }
        }else{
            Timber.e("Fetch Failed")

        }
    }

    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refresh(searchBy: String,query: String): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        api.searchMoviesCall(
            apiKey = ApiInterface.API_KEY,
            searchString = query,
            type = searchBy,
            page = "1"
        ).enqueue(

            object : Callback<MovieResult> {
                override fun onFailure(call: Call<MovieResult>, t: Throwable) {
                    // retrofit calls this on main thread so safe to call set value
                    Timber.e("NFailed ")
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(
                    call: Call<MovieResult>,
                    response: Response<MovieResult>
                ) {
                    Timber.e("NFailed ${response.toString()}")
                    if (response.body()!!.Response.equals("False", ignoreCase = true)) {
                        Timber.e("State failed")
                        val errorMessage = response.body()!!.Error
                        Timber.e("State failed $errorMessage")
                        if (errorMessage.isNullOrEmpty()) {
                            networkState.value = NetworkState.error(NO_DATA_FOUND)
                        } else {
                            networkState.value = NetworkState.error(errorMessage)
                        }
                    } else {
                        ioExecutor.diskIO().execute {
                            db.runInTransaction {
                                db.movieDao().clearMovies(searchBy+query)
                                insertResultIntoDb(searchBy+query, response.body())
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    }
                }
            }
        )
        return networkState
    }

    /**
     * Returns a Listing for the given search query.
     */
    @MainThread
    fun searchOMDB(searchBy:String,query: String, pageSize: Int): MovieList<Movie> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = MovieBoundaryCallback(
            webservice = api,
            searchBy = searchBy,
            searchQuery = query,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor
        )
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = refreshTrigger.switchMap {
            refresh(searchBy,query)
        }
        // We use toLiveData Kotlin extension function here, you could also use LivePagedListBuilder
        val livePagedList = db.movieDao().moviesBySearchQuery(searchBy+query).toLiveData(
            pageSize = pageSize,
            boundaryCallback = boundaryCallback
        )
        val dataState = boundaryCallback.dataState
        return MovieList(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState,
            dataState = dataState
        )
    }
}


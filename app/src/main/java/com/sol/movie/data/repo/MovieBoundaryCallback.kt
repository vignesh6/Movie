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
import androidx.paging.PagedList
import com.sol.movie.api.ApiInterface
import com.sol.movie.data.NetworkState
import com.sol.movie.module.search.data.Movie
import com.sol.movie.module.search.data.MovieResult
import com.sol.movie.util.AppExecutors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class MovieBoundaryCallback(
    private val searchBy: String,
    private val searchQuery: String,
    private val webservice: ApiInterface,
    private val handleResponse: (String, MovieResult?) -> Unit,
    private val ioExecutor: AppExecutors
) : PagedList.BoundaryCallback<Movie>() {

    private val _dataState: MutableLiveData<NetworkState> = MutableLiveData()
    val dataState: LiveData<NetworkState> = _dataState
    val helper = PagingRequestHelper(ioExecutor.diskIO())
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice.searchMoviesCall(
                type = searchBy,
                searchString = searchQuery,
                page = "1",
                apiKey = ApiInterface.API_KEY
            ).enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Movie) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webservice.searchMoviesCall(
                type = searchBy,
                searchString = searchQuery,
                apiKey = ApiInterface.API_KEY,
                page = (itemAtEnd.page + 1).toString()
            )
                .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
        response: Response<MovieResult>,
        it: PagingRequestHelper.Request.Callback
    ) {
        ioExecutor.diskIO().execute {
            handleResponse(searchBy + searchQuery, response.body())
            it.recordSuccess()
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Movie) {
        // ignored, since we only ever append to what's in the DB
    }

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<MovieResult> {
        return object : Callback<MovieResult> {
            override fun onFailure(
                call: Call<MovieResult>,
                t: Throwable
            ) {
                it.recordFailure(t)
            }

            override fun onResponse(
                call: Call<MovieResult>,
                response: Response<MovieResult>
            ) {
                //Handle response from api, update database if respons has data else update UI with appropriate message
                if (response.body()!!.Response.equals("False", ignoreCase = true)) {
                    val errorMessage = response.body()!!.Error
                    if (errorMessage.isNullOrEmpty()) {
                        _dataState.value = NetworkState.error(NO_DATA_FOUND)
                    } else {
                        _dataState.value = NetworkState.error(errorMessage)
                    }
                } else {
                    insertItemsIntoDb(response, it)
                }
            }
        }
    }
}
package com.sol.movie.ui.search

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.paging.*
import com.sol.movie.di.CoroutineScropeIO
import com.sol.movie.ui.data.Movie
import com.sol.movie.ui.data.MovieRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
const val DEBOUNCE_TIME_IN_MILLISECONDS = 300L
const val MIN_QUERY_LENGTH = 3

class MovieViewModel @Inject constructor(private val repository: MovieRepository):ViewModel() {

    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Movie>>? = null

    fun searchRepo(queryString: String): Flow<PagingData<Movie>> {
        val lastResult = currentSearchResult
        if (queryString == currentQueryValue && lastResult != null) {
            Timber.e("retuned here")
            return lastResult
        }
        currentQueryValue = queryString
        val dbResult: PagingSource<Int, Movie> = repository.getOfflineData(queryString)

        val newResult: Flow<PagingData<Movie>> = repository.getSearchResultStream(queryString).cachedIn(viewModelScope)
        Timber.e("result $newResult")
        currentSearchResult = newResult
        return newResult
    }
}

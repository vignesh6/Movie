package com.sol.movie.ui.search

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.sol.movie.di.CoroutineScropeIO
import com.sol.movie.ui.data.Movie
import com.sol.movie.ui.data.MovieRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
const val DEBOUNCE_TIME_IN_MILLISECONDS = 300L
const val MIN_QUERY_LENGTH = 3
class MovieViewModel @Inject constructor(private val repository: MovieRepository, @CoroutineScropeIO private val ioCoroutineScope: CoroutineScope):ViewModel() {

    private val searchQuery:MutableLiveData<String> = MutableLiveData()

    fun searchMovies(query:String){
        searchQuery.postValue(query)
    }
    val movies by lazy {
        repository.observePagedMovies(true, "batman", ioCoroutineScope)
    }


    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val queryChannel = BroadcastChannel<String>(Channel.CONFLATED)

    @FlowPreview
    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val internalSearchResult = queryChannel
        .asFlow()
        .debounce(DEBOUNCE_TIME_IN_MILLISECONDS)
        .mapLatest {
            try {
                if (it.length >= MIN_QUERY_LENGTH) {
                    val searchResult = withContext(Dispatchers.IO) {
                        repository.observePagedMovies(true,it,coroutineScope = ioCoroutineScope)
                    }
                    if (!searchResult.value.isNullOrEmpty()) {
                        ValidResult(searchResult)
                    } else {
                        EmptyResult
                    }
                } else {
                    EmptyQuery
                }
            } catch (e: Throwable) {
                if (e is CancellationException) {
                    println("Search was cancelled!")
                    throw e
                } else {
                    ErrorResult(e)
                }
            }
        }
        .catch { it: Throwable -> emit(TerminalError) }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val searchResult = internalSearchResult.asLiveData()
}

sealed class SearchResult
class ValidResult(val result: LiveData<PagedList<Movie>>) : SearchResult()
object EmptyResult : SearchResult()
object EmptyQuery : SearchResult()
class ErrorResult(val e: Throwable) : SearchResult()
object TerminalError : SearchResult()
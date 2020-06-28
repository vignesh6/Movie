package com.sol.movie.module.search.ui

import androidx.lifecycle.*
import com.sol.movie.data.repo.MoviesDBRepository
import timber.log.Timber
import javax.inject.Inject
const val PAGE_SIZE = 10
class MovieViewModel @Inject constructor(
    private val repository: MoviesDBRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    //Mutable live data that holds search type or category such as movies,episode or series
    private val searchType: MutableLiveData<String> = MutableLiveData()
    //Mutable live data that holds value enetered by user
    private val queryString: MutableLiveData<String> = MutableLiveData()
    init {
        searchType.value = ""
    }

    //Paged list observer returns data from MoviesDBRepository repository
    private val repoResult = savedStateHandle.getLiveData<String>(KEY_SEACRHQUERY).map {
       Timber.e("$it ${searchType.value}")
            val searchBy = it.substring(0, searchType.value!!.length)
            val query = it.substring(searchType.value!!.length, it.length)
            Timber.e("$searchBy $query")
            repository.searchOMDB(searchBy, query, PAGE_SIZE)
    }
    val movies = repoResult.switchMap { it.pagedList }
    val networkState = repoResult.switchMap { it.networkState }
    val refreshState = repoResult.switchMap { it.refreshState }
    val dataState = repoResult.switchMap { it.dataState }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun showSearchItem(query: String): Boolean {
        queryString.value = query
        val searchQuery = searchType.value + query
        if (savedStateHandle.get<String>(KEY_SEACRHQUERY) == searchQuery) {
            return false
        }
        savedStateHandle.set(KEY_SEACRHQUERY, searchQuery)
        return true
    }

    fun setSearchType(type: String) {
        searchType.value = type
        if (queryString.value != null) {
            val searchQuery = searchType.value + queryString.value
            if (savedStateHandle.get<String>(KEY_SEACRHQUERY) != searchQuery) {
                savedStateHandle.set(KEY_SEACRHQUERY, searchQuery)
            }
        }
    }

    fun retry() {
        val listing = repoResult.value
        listing?.retry?.invoke()
    }

    companion object {
        const val KEY_SEACRHQUERY = "searchquery"
        const val DEFAULT_SEARCHQUERY = ""
    }
}

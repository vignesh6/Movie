package com.sol.movie.ui.data

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class MoviePageDataSourceFactory @Inject constructor(private val query:String,private val dataSource: MovieRemoteDataSource,private val dao: MovieDao,private val scope: CoroutineScope) : DataSource.Factory<Int, Movie>() {
    private val liveData = MutableLiveData<MoviePageDataSource>()

    override fun create(): DataSource<Int, Movie> {
        val source = MoviePageDataSource(query, dataSource, dao, scope)
        liveData.postValue(source)
        return source
    }

    companion object {
        private const val PAGE_SIZE = 100
        fun pagedListConfig() = PagedList.Config.Builder()
            .setInitialLoadSizeHint(PAGE_SIZE)
            .setPageSize(PAGE_SIZE)
            .setEnablePlaceholders(true)
            .build()
    }
}
package com.sol.movie.ui.data

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class MovieRepository @Inject constructor(private val movieDao: MovieDao,private val movieRemoteDataSource: MovieRemoteDataSource) {
     fun observePagedMovies(connectivityAvailable: Boolean, query: String? = null,
                         coroutineScope: CoroutineScope
    ) =
        if (connectivityAvailable) observeRemotePagedMovies(query, coroutineScope)
        else observeLocalPagedMovies(query)

    private fun observeLocalPagedMovies(query: String?): LiveData<PagedList<Movie>> {
        val dataSourceFactory =
            if (query == null) movieDao.getPagedMovies()
            else movieDao.getPagedMovieByTittle(query)

        return LivePagedListBuilder(dataSourceFactory,
            MoviePageDataSourceFactory.pagedListConfig()).build()
    }

    private fun observeRemotePagedMovies(query: String?, coroutineScope: CoroutineScope): LiveData<PagedList<Movie>> {
        val dataSourceFactory = MoviePageDataSourceFactory(query!!, movieRemoteDataSource,
            movieDao, coroutineScope)
        return LivePagedListBuilder(dataSourceFactory,
            MoviePageDataSourceFactory.pagedListConfig()).build()
    }
}
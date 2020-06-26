package com.sol.movie.ui.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.*
import com.sol.movie.api.ApiInterface
import com.sol.movie.data.AppDatabase
import com.sol.movie.data.MovieRemoteMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepository @Inject constructor(private val database: AppDatabase,private val apiService: ApiInterface) {
    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */
    fun getSearchResultStream(query: String): Flow<PagingData<Movie>> {
        Log.d("Movie Repository", "New query: $query")

        // appending '%' so we can allow other characters to be before and after the query string
        val dbQuery = "%${query.replace(' ', '%')}%"
        val pagingSourceFactory = { database.movieDao().getPagedMovieByTittle(dbQuery) }

        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE),
            remoteMediator = MovieRemoteMediator(
                query,
                apiService,
                database
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }
    fun getOfflineData(query: String): PagingSource<Int,Movie> {
        val dbQuery = "%${query.replace(' ', '%')}%"
      return database.movieDao().getPagedMovieByTittle(dbQuery)
    }
    companion object {
        private const val NETWORK_PAGE_SIZE = 10
    }
}
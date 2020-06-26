package com.sol.movie.data

import android.accounts.NetworkErrorException
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.sol.movie.api.ApiInterface
import com.sol.movie.ui.data.Movie
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.io.InvalidObjectException

private val MOVIE_STARTING_PAGE_INDEX: Int = 1

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator(private val query:String,private val service:ApiInterface,private val appDatabase: AppDatabase) : RemoteMediator<Int, Movie>() {


    override suspend fun load(loadType: LoadType, state: PagingState<Int, Movie>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                Timber.e("REFRESH")
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: MOVIE_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                Timber.e("PREPEND")
                val remoteKeys = getRemoteKeyForFirstItem(state)
                if (remoteKeys == null) {
                    // The LoadType is PREPEND so some data was loaded before,
                    // so we should have been able to get remote keys
                    // If the remoteKeys are null, then we're an invalid state and we have a bug
                    throw InvalidObjectException("Remote key and the prevKey should not be null")
                }
                // If the previous key is null, then we can't request more data
                val prevKey = remoteKeys.prevKey
                if (prevKey == null) {
                    Timber.e("ENd of page")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKeys.prevKey
            }
            LoadType.APPEND -> {
                Timber.e("APPEND")
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys == null || remoteKeys.nextKey == null) {
                    throw InvalidObjectException("Remote key should not be null for $loadType")
                }
                remoteKeys.nextKey
            }
        }
        try {
            val apiResponse = service.searchMovies(ApiInterface.TYPE,ApiInterface.API_KEY,page.toString(), query)
            Timber.e("movie ${apiResponse.Response}")
            val movies = if(apiResponse.Response .equals("True",ignoreCase = true)) {
                Timber.e("movie")
                apiResponse.Search
            }else{
                Timber.e("movie empty")
                emptyList()
            }
            val endOfPaginationReached = movies.isNullOrEmpty()
            appDatabase.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    appDatabase.remoteKeysDao().clearRemoteKeys()
                   // appDatabase.movieDao().clearMovies()
                }
                val prevKey = if (page == MOVIE_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = movies.map {
                    RemoteKeys(movieId = it.imdbID, prevKey = prevKey, nextKey = nextKey)
                }
                Timber.e("Insert $prevKey $nextKey")
                Timber.e("Insert movie ${movies.size}")
                appDatabase.remoteKeysDao().insertAll(keys)
                appDatabase.movieDao().insertAll(movies)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            Timber.e("$exception")
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            Timber.e("htt $exception")
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Movie>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { movie ->
                // Get the remote keys of the last item retrieved
                appDatabase.remoteKeysDao().remoteKeysRepoId(movie.imdbID)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Movie>): RemoteKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { movie ->
                // Get the remote keys of the first items retrieved
                appDatabase.remoteKeysDao().remoteKeysRepoId(movie.imdbID)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Movie>
    ): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.imdbID?.let { movieId ->
                appDatabase.remoteKeysDao().remoteKeysRepoId(movieId)
            }
        }
    }
}
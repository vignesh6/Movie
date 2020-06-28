package com.sol.movie.module.search.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie")
data class Movie(
    @PrimaryKey
    val imdbID: String,
    val Title: String,
    val Poster: String,
    val Type: String,
    val Year: String
){
    // to be consistent w/ changing backend order, we need to keep a data like this
    var indexInResponse: Int = -1
    var searchQuery: String = ""
    var page:Int = 1
}
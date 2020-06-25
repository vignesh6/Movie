package com.sol.movie.ui.data

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
)
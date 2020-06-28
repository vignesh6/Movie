package com.sol.movie.module.detail.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detail")
data class MovieDetail(
    @PrimaryKey
    val Title: String,
    val Year: String,
    val Rated: String,
    val Released: String,
    val Genre: String,
    val Plot: String,
    val Language: String,
    val Poster: String
)
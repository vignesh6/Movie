package com.sol.movie.module.search.data

data class MovieResult(
    val Response: String,
    val Search: List<Movie> = emptyList(),
    val totalResults: String,
    val Error:String?
)
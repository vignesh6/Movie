package com.sol.movie.ui.data

data class MovieResult(
    val Response: String,
    val Search: List<Movie> = emptyList(),
    val totalResults: String
)
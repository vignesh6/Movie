package com.sol.movie.di


import com.sol.movie.ui.search.SearchMovieFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {
    @ContributesAndroidInjector
    abstract fun contributeSearchMovieFragment(): SearchMovieFragment
}
package com.sol.movie.di


import com.sol.movie.module.detail.ui.DetailFragment
import com.sol.movie.module.search.ui.SearchMovieFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {
    @ContributesAndroidInjector
    abstract fun contributeSearchMovieFragment(): SearchMovieFragment

    @ContributesAndroidInjector
    abstract fun contributeDetailFragment(): DetailFragment
}
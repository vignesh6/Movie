package com.sol.movie.di


import com.sol.movie.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun bindMainActivity(): MainActivity
}
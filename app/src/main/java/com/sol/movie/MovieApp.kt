package com.sol.movie

import android.app.Application
import com.facebook.stetho.Stetho
import com.sol.movie.di.AppComponent
import com.sol.movie.di.AppInjector
import com.sol.movie.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import fr.dasilvacampos.network.monitoring.ConnectivityStateHolder.registerConnectivityBroadcaster
import timber.log.Timber
import javax.inject.Inject

class MovieApp : Application(), HasAndroidInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    private lateinit var appComponent: AppComponent
    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
        appComponent.inject(this)
        AppInjector.init(this)
         if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Stetho.initializeWithDefaults(this)
        registerConnectivityBroadcaster()
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
}
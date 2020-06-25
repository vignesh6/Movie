package com.sol.movie.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.sol.movie.BuildConfig
import com.sol.movie.api.ApiInterface
import com.sol.movie.data.AppDatabase
import com.sol.movie.ui.data.MovieRemoteDataSource
import com.sol.movie.util.AppConstant

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideStethoIntercepor(): StethoInterceptor {
        return StethoInterceptor()
    }
    @Provides
    @Singleton
    fun provideSharedPreference(context: Context): SharedPreferences {
        return context.getSharedPreferences(AppConstant.PREFERENCE_NAME,Context.MODE_PRIVATE)
    }
    @Singleton
    @Provides
    fun provideApiInterface(
        okhttpClient: OkHttpClient,
        converterFactory: GsonConverterFactory
    ) = provideService(okhttpClient, converterFactory, ApiInterface::class.java)

   /* @Provides
    @Singleton
    fun provideFactsRemoteDataSource(apiInterface: ApiInterface) =
        FactsRemoteDataSource(apiInterface)*/

    @Singleton
    @Provides
    fun provideDb(app: Application) = AppDatabase.getInstance(app)

    @Singleton
    @Provides
    fun provideMovieDao(db: AppDatabase) = db.movieDao()
    @Singleton
    @Provides
    fun provideMovieRemoteDataSource(service: ApiInterface)
            = MovieRemoteDataSource(service)
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        cache: Cache,
        stetho: StethoInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(stetho)
            .cache(cache)
            .build()
    }


    @Provides
    fun provideLoggingInterceptor() =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideGsonConverterFactory(gson: Gson): GsonConverterFactory =
        GsonConverterFactory.create(gson)

    @Provides
    @Singleton
    fun provideCache(context: Context): Cache {
        val cacheSize = 5 * 1024 * 1024 // 5 MB
        val cacheDir = context.cacheDir
        return Cache(cacheDir, cacheSize.toLong())
    }

    private fun createRetrofit(
        okhttpClient: OkHttpClient,
        converterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiInterface.API_END_POINT_URL)
            .client(okhttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }

    private fun <T> provideService(
        okhttpClient: OkHttpClient,
        converterFactory: GsonConverterFactory, clazz: Class<T>
    ): T {
        return createRetrofit(okhttpClient, converterFactory).create(clazz)
    }
    @CoroutineScropeIO
    @Provides
    fun provideCoroutineScopeIO() = CoroutineScope(Dispatchers.IO)
}
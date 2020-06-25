package com.sol.movie.data

import android.content.SharedPreferences
import javax.inject.Inject

class AppSharedPreference @Inject constructor(private val sharedPreference: SharedPreferences){
    fun getStringData(key: String) = sharedPreference.getString(key,"")
    fun putStringData(key: String,data:String) =  sharedPreference.edit().putString(key,data).apply()
}
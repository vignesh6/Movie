package com.sol.movie.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sol.movie.R
import timber.log.Timber


object BindingUtil {
    @JvmStatic
    @BindingAdapter("setPosterImage")
    fun setPosterImage(view: ImageView, url: String) {
        try {
            if(!url.equals("N/A")) {

                Glide.with(view.context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_local_movies_24).into(view)
            }else{
                Timber.e("N?A")
                Glide.with(view.context).load(R.drawable.movie_poster).into(view)
            }
        } catch (e: Exception) {
            Glide.with(view.context).load(R.drawable.ic_local_movies_24).centerCrop().into(view)
        }
    }
}
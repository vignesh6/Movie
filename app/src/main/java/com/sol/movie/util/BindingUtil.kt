package com.sol.movie.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.sol.movie.R


object BindingUtil {
    @JvmStatic
    @BindingAdapter("setPosterImage")
    fun setPosterImage(view: ImageView, url: String) {
        try {
            if (!url.equals("N/A")) {
                Glide.with(view.context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_local_movies_24).into(view)
            } else {
                Glide.with(view.context).load(R.drawable.movie_poster).into(view)
            }
        } catch (e: Exception) {
            Glide.with(view.context).load(R.drawable.ic_local_movies_24).centerCrop().into(view)
        }
    }

    @JvmStatic
    @BindingAdapter("setGenre")
    fun setGenre(view: ChipGroup, genreStr: String) {
        if (genreStr.isNotEmpty()) {
            val genres = genreStr.split(",")
            val inflater = view.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            for (genre in genres) {
                val chip = inflater.inflate(R.layout.chip_genre, view, false) as Chip
                chip.text = genre
                view.addView(chip)
            }
        }
    }
}
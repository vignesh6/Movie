

package com.sol.movie.ui.search

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sol.movie.R
import com.sol.movie.databinding.ItemMovieBinding
import com.sol.movie.ui.data.Movie

/**
 * View Holder for a [Movie] RecyclerView list item.
 */
class MovieViewHolder(private val itemMovieBinding: ItemMovieBinding) : RecyclerView.ViewHolder(itemMovieBinding.root) {

    private var movie: Movie? = null

    fun bind(listener:View.OnClickListener,item: Movie?) {
        itemMovieBinding.apply {
            clickListener = listener
            movie = item
           // executePendingBindings()
        }
    }


}



package com.sol.movie.module.search.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.sol.movie.databinding.ItemMovieBinding
import com.sol.movie.module.search.data.Movie

/**
 * View Holder for a [Movie] RecyclerView list item.
 */
class MovieViewHolder(private val itemMovieBinding: ItemMovieBinding) : RecyclerView.ViewHolder(itemMovieBinding.root) {
    fun bind(listener:View.OnClickListener,item: Movie?) {
        itemMovieBinding.apply {
            clickListener = listener
            movie = item
            executePendingBindings()
        }
    }


}

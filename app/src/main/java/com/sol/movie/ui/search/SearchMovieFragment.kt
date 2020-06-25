package com.sol.movie.ui.search

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sol.movie.R
import com.sol.movie.databinding.FragmentSearchBinding
import com.sol.movie.di.Injectable
import com.sol.movie.util.getQueryTextChangeStateFlow
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class SearchMovieFragment : Fragment(), Injectable {
    private lateinit var viewModel: MovieViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSearchBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MovieViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        viewModel.movies.observe(viewLifecycleOwner, Observer {
            Timber.e(it.toString())
        })
        lifecycleScope.launch(Dispatchers.Main) {
            searchViewTxt.getQueryTextChangeStateFlow().debounce(300).filter { query ->
                if (query.isEmpty()) {
                    return@filter false
                } else {
                    viewModel.queryChannel.send(query)
                    return@filter true
                }
            }
        }


    }

    @ExperimentalCoroutinesApi
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        lifecycleScope.launch {
            searchView.getQueryTextChangeStateFlow().debounce(300).filter { query ->
                if (query.isEmpty()) {
                    return@filter false
                } else {
                    viewModel.queryChannel.send(query)
                    return@filter true
                }
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isEmpty()) {
                    return false
                } else {
                    viewModel.searchMovies(query)
                    /*lifecycleScope.launch(Dispatchers.Main) {
                        viewModel.queryChannel.send(query)
                    }*/
                    return true
                }
                return false
            }

        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}
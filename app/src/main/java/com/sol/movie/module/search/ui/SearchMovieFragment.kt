package com.sol.movie.module.search.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sol.movie.R
import com.sol.movie.data.NetworkState
import com.sol.movie.databinding.FragmentSearchBinding
import com.sol.movie.di.Injectable
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import javax.inject.Inject


class SearchMovieFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MovieViewModel
    private lateinit var binding: FragmentSearchBinding
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: MoviesAdapter
    private var searchBy: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MovieViewModel::class.java)
        layoutManager = LinearLayoutManager(activity)
        setLayoutManager(binding)
        adapter = MoviesAdapter {
            viewModel.retry()
        }
        binding.list.adapter = this.adapter
        return binding.root
    }

    private fun initViews() {
        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decoration)
        val categories = listOf("Movie", "Series", "Episode")
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            categories
        )
        spinnerCategory.adapter = categoryAdapter
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                searchBy = getString(R.string.movie)
                binding.inputLayout.hint = getString(R.string.search_hint, searchBy)
                viewModel.setSearchType(searchBy)
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val categorySelected = p0!!.getItemAtPosition(p2).toString()
                binding.inputLayout.hint = getString(R.string.search_hint, categorySelected)
                searchBy = categorySelected[0].toLowerCase() + categorySelected.substring(1)
                viewModel.setSearchType(searchBy)
            }

        }
        initAdapter()
        initSearch()
        initSwipeToRefresh()
    }

    private fun initAdapter() {
        viewModel.movies.observe(viewLifecycleOwner, Observer {
            lifecycleScope.launch(Dispatchers.Main) {
                adapter.submitList(it)
            }
        })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
        viewModel.dataState.observe(viewLifecycleOwner, Observer {
            if (adapter.currentList != null) {
                adapter.currentList?.let { data ->
                    if (data.size == 0) {
                        adapter.setNetworkState(null)
                        txtNoData.visibility = View.VISIBLE
                        txtNoData.text = it.msg
                    } else {
                        adapter.setNetworkState(null)
                    }
                }
            }
        })

    }

    private fun setLayoutManager(binding: @NotNull FragmentSearchBinding) {
        val recyclerView = binding.list
        var scrollPosition = 0
        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.layoutManager != null) {
            scrollPosition = (recyclerView.layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.scrollToPosition(scrollPosition)
    }

    private fun initSearch() {
        binding.searchRepo.doOnTextChanged { text, _, _, _ ->
            val searchText = text.toString().trim()
            lifecycleScope.launch {
                if (searchText.length >= 3) {
                    delay(300)
                    withContext(Dispatchers.Main) {
                        updateRepoListFromInput()
                    }
                }
            }

        }
        val watcher = object : TextWatcher {
            private var searchFor = ""
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText == searchFor || searchText.length <= 3)
                    return
                searchFor = searchText
                lifecycleScope.launch {
                    delay(300)  //debounce timeOut
                    if (searchText != searchFor)
                        return@launch
                    updateRepoListFromInput()
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit
        }
        binding.searchRepo.addTextChangedListener(watcher)
        binding.searchRepo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }
        binding.searchRepo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }
    }

    private fun updateRepoListFromInput() {
        binding.searchRepo.text.trim().toString().let {
            if (it.isNotEmpty()) {
                if (viewModel.showSearchItem(it)) {
                    txtNoData.visibility = View.GONE
                    list.scrollToPosition(0)
                    (list.adapter as? MoviesAdapter)?.submitList(null)
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initViews()
    }

    private fun initSwipeToRefresh() {
        viewModel.refreshState.observe(viewLifecycleOwner, Observer {
            swipeRefresh.isRefreshing = it == NetworkState.LOADING
        })
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

}
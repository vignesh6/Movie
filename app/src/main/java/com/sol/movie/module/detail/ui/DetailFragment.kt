package com.sol.movie.module.detail.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.onNavDestinationSelected
import com.sol.movie.data.repo.Resource
import com.sol.movie.databinding.FragmentDetailBinding
import com.sol.movie.di.Injectable
import com.sol.movie.module.detail.data.MovieDetail
import fr.dasilvacampos.network.monitoring.ConnectivityStateHolder
import fr.dasilvacampos.network.monitoring.Event
import fr.dasilvacampos.network.monitoring.NetworkEvents
import kotlinx.android.synthetic.main.fragment_detail.*
import timber.log.Timber
import javax.inject.Inject

class DetailFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentDetailBinding
    private lateinit var viewModel: DetailViewModel
    private var isConnected = false
    private val args: DetailFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController()
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        viewModel.setTittle(args.Tittle)
        viewModel.movieDetail.observe(viewLifecycleOwner, Observer {
            handleResponse(it)
        })
        retry_button.setOnClickListener{
            viewModel.retry()
            it.visibility = View.GONE
            txtNoInternet.visibility = View.GONE
        }
        NetworkEvents.observe(viewLifecycleOwner, Observer {
            if (it is Event.ConnectivityEvent)
                handleConnectivityChange()
        })
    }

    private fun handleConnectivityChange() {
        isConnected = ConnectivityStateHolder.isConnected
    }

    private fun handleResponse(it: Resource<MovieDetail>?) {
        when (it!!.status) {
            Resource.Status.LOADING -> {
                progress_bar.visibility = View.VISIBLE
            }
            Resource.Status.SUCCESS -> {
                progress_bar.visibility = View.GONE
                viewModel.updateUI(it.data)
            }
            Resource.Status.ERROR -> {
                progress_bar.visibility = View.GONE
                retry_button.visibility = View.VISIBLE
                if(!isConnected){
                    txtNoInternet.visibility = View.VISIBLE
                }
            }
        }
    }
}
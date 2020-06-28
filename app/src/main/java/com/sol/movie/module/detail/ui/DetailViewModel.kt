package com.sol.movie.module.detail.ui

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.sol.movie.api.ApiInterface
import com.sol.movie.data.repo.AbsentLiveData
import com.sol.movie.data.repo.DetailRepository
import com.sol.movie.data.repo.Resource
import com.sol.movie.module.detail.data.MovieDetail
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DetailViewModel @Inject constructor(private val repository: DetailRepository) : ViewModel() {
    private val _title:MutableLiveData<String> = MutableLiveData()

     var title:ObservableField<String> = ObservableField()
     var rating:ObservableField<String> = ObservableField()
     var year:ObservableField<String> = ObservableField()
     var released:ObservableField<String> = ObservableField()
     var language:ObservableField<String> = ObservableField()
     var genre:ObservableField<String> = ObservableField()
     var plot:ObservableField<String> = ObservableField()
     var posterImage:ObservableField<String> = ObservableField()


    fun setTittle(title: String) {
        _title.value= title
    }

    fun updateUI(data: MovieDetail?) {
        title.set(data!!.Title)
        year.set(data.Year)
        rating.set(data.Rated)
        released.set(data.Released)
        plot.set(data.Plot)
        genre.set(data.Genre)
        language.set(data.Language)
        posterImage.set(data.Poster)
    }

    val movieDetail: LiveData<Resource<MovieDetail>> = _title.switchMap { title ->
        if (title == null) {
            AbsentLiveData.create()
        } else {
            repository.getMovieDetail(title)
        }
    }
    fun retry() {
        _title.value?.let {
            _title.value = it
        }
    }
}
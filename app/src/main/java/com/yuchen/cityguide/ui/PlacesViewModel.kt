package com.yuchen.cityguide.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableList
import android.location.Location
import android.util.Log
import com.yuchen.cityguide.R
import com.yuchen.cityguide.data.Place
import com.yuchen.cityguide.data.PlaceType
import com.yuchen.cityguide.data.PlacesRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider


/**
 * Created by yuchen on 1/6/18.
 */
class PlacesViewModel(
        context: Application,
        private val placesRepository: PlacesRepository
) : AndroidViewModel(context) {

    private val isDataLoadingError = ObservableBoolean(false)

    val items: ObservableList<Place> = ObservableArrayList()
    val dataLoading = ObservableBoolean(false)
    val currentFilteringLabel = ObservableField<String>()
    val empty = ObservableBoolean(false)

    var currentFiltering = PlaceType.BARS
        set(value) {
            field = value
            updateFiltering()
        }

    fun start(location: Location) {
        loadPlaces(false, location)
    }

    fun loadPlaces(forceUpdate: Boolean, location: Location) {
        loadPlaces(forceUpdate, true, location)
    }

    fun updateFiltering() {
        when (currentFiltering) {
            PlaceType.BARS -> {
                setFilter(R.string.label_bars)
            }
            PlaceType.BISTROS -> {
                setFilter(R.string.label_bistros)
            }
            PlaceType.CAFES -> {
                setFilter(R.string.label_cafés)
            }
        }
    }

    private fun setFilter(filteringLabelString: Int) {
        val context = getApplication() as Context
        currentFilteringLabel.set(context.getString(filteringLabelString))
    }

    private fun loadPlaces(forceUpdate: Boolean, showLoadingUI: Boolean, location: Location) {
        if (showLoadingUI) {
            dataLoading.set(true)
        }
        if (forceUpdate) {
            placesRepository.refreshPlaces()
        }

        placesRepository.getPlaces(location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(     // onNext
                        { places ->
                            val placesToShow: List<Place>

                            when (currentFiltering) {
                                PlaceType.BARS ->
                                    placesToShow = places.filter { it.type == PlaceType.BARS }
                                PlaceType.BISTROS ->
                                    placesToShow = places.filter { it.type == PlaceType.BISTROS }
                                PlaceType.CAFES ->
                                    placesToShow = places.filter { it.type == PlaceType.CAFES }
                            }

                            if (showLoadingUI) {
                                dataLoading.set(false)
                            }
                            isDataLoadingError.set(false)

                            with(items) {
                                clear()
                                addAll(placesToShow)
                                empty.set(isEmpty())
                            }
                        },
                        // onError
                        { throwable ->
                            kotlin.run {
                                isDataLoadingError.set(true)
                                Log.v(TAG, "there is error " +
                                        "$throwable")
                                //todo show error on ui
                            }
                        })


    }

    companion object {
        private val TAG = "PlacesViewModel"
    }


    class Factory(private val application: Application, private val placesRepository: PlacesRepository) : ViewModelProvider
    .NewInstanceFactory() {


        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return PlacesViewModel(application, placesRepository) as T
        }
    }
}
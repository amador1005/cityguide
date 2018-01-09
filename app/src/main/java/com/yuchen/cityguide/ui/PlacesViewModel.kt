package com.yuchen.cityguide.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableList
import android.util.Log
import com.yuchen.cityguide.data.Place
import com.yuchen.cityguide.data.PlaceType
import com.yuchen.cityguide.data.PlacesRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import io.reactivex.Scheduler


/**
 * Created by yuchen on 1/6/18.
 */
class PlacesViewModel(
        context: Application,
        private val placesRepository: PlacesRepository,
        private val subscribeOn: Scheduler,
        private val observeOn: Scheduler
) : AndroidViewModel(context) {

    private val isDataLoadingError = ObservableBoolean(false)

    val items: ObservableList<Place> = ObservableArrayList()
    val dataLoading = ObservableBoolean(false)
    val empty = ObservableBoolean(false)

    var currentFiltering = PlaceType.BAR

    fun loadPlaces(forceUpdate: Boolean, showLoadingUI: Boolean, location: PlacesRepository.PlaceLocation) {
        if (showLoadingUI) {
            dataLoading.set(true)
        }
        if (forceUpdate) {
            placesRepository.refreshPlaces()
        }

        placesRepository.getPlaces(location)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(     // onNext
                        { places ->
                            val filteredPlaces: List<Place>
                            when (currentFiltering) {
                                PlaceType.BAR ->
                                    filteredPlaces = places.filter { it.type == PlaceType.BAR }
                                PlaceType.BISTRO ->
                                    filteredPlaces = places.filter { it.type == PlaceType.BISTRO }
                                PlaceType.CAFE ->
                                    filteredPlaces = places.filter { it.type == PlaceType.CAFE }
                            }

                            if (showLoadingUI) {
                                dataLoading.set(false)
                            }
                            isDataLoadingError.set(false)
                            with(items) {
                                clear()
                                addAll(filteredPlaces)
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
        private val TAG = javaClass.simpleName
    }


    class Factory(private val application: Application, private val placesRepository: PlacesRepository) : ViewModelProvider
    .NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return PlacesViewModel(application, placesRepository, Schedulers.io(),
                    AndroidSchedulers.mainThread()) as T
        }
    }
}
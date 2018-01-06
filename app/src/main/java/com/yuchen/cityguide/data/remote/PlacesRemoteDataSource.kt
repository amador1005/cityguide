package com.yuchen.cityguide.data.remote

import android.location.Location
import android.util.Log
import com.yuchen.cityguide.data.PlacesDataSource
import io.reactivex.Observable
import com.yuchen.cityguide.data.Place
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by yuchen on 1/5/18.
 */
class PlacesRemoteDataSource : PlacesDataSource {

    private val mService: PlaceService

    constructor() {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.create())
                .addConverterFactory(
                        GsonConverterFactory.create())
                .baseUrl("https://maps.googleapis.com")
                .build()
        mService = retrofit.create(PlaceService::class.java)

    }

    private fun fetchPlaces(type: String, location: Location): List<Place> {
        val location = "%f,%f".format(location.latitude,
                location.longitude)
        Log.d(TAG, "location $location")
        return mService
                .fetchPlaces(types = type, key = KEY, location = location)
                .flatMap { response ->
                    Observable.fromIterable(response.results)
                            .doOnNext { place -> place.type = type }
                }.toList().blockingGet()

    }

    override fun fetchPlaces(location: Location): Observable<List<Place>> {
        return Observable.fromCallable {
            val cafeList = fetchPlaces("cafe", location)
            Log.v(TAG, "cafeList $cafeList")
            val barList = fetchPlaces("bar", location)
            val restaurantList = fetchPlaces("restaurant", location)

            val combinedList = mutableListOf<Place>()
            combinedList.addAll(cafeList)
            combinedList.addAll(barList)
            combinedList.addAll(restaurantList)
            combinedList
        }
    }


    companion object {
        private val TAG = "PlacesRemoteDataSource"
        private val KEY = "AIzaSyA670Z60cbgYkmfn4J4CH129btHEXHjEIY"
    }
}
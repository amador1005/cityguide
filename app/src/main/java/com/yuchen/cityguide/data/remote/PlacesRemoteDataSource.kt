package com.yuchen.cityguide.data.remote

import android.location.Location
import android.util.Log
import com.yuchen.cityguide.data.PlacesDataSource
import io.reactivex.Observable
import com.yuchen.cityguide.data.Place
import com.yuchen.cityguide.data.PlaceType
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor




/**
 * Created by yuchen on 1/5/18.
 */
class PlacesRemoteDataSource : PlacesDataSource {

    private val placeService: PlaceService

    constructor() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.create())
                .addConverterFactory(
                        GsonConverterFactory.create())
                .baseUrl("https://maps.googleapis.com")
                .build()
        placeService = retrofit.create(PlaceService::class.java)

    }

    private fun fetchPlaces(type: String, placeType: PlaceType, location: Location): List<Place> {
        val location = "%f,%f".format(location.latitude,
                location.longitude)
        Log.d(TAG, "location $location")
        return placeService
                .fetchPlaces(types = type, key = KEY, location = location)
                .flatMap { response ->
                    Observable.fromIterable(response.results)
                            .doOnNext { place -> place.type = placeType }
                }.toList().blockingGet()

    }

    override fun fetchPlaces(location: Location): Observable<List<Place>> {
        return Observable.fromCallable {
            val cafeList = fetchPlaces("cafe", PlaceType.CAFES, location)
            val barList = fetchPlaces("bar", PlaceType.BARS, location)
            val restaurantList = fetchPlaces("restaurant", PlaceType.BISTROS, location)
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
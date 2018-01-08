package com.yuchen.cityguide.data.remote

import com.yuchen.cityguide.data.DistanceResponse
import com.yuchen.cityguide.data.PlaceResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * Created by yuchen on 1/6/18.
 */
interface PlaceService {

    @GET("maps/api/place/search/json?radius=5000&sensor=false")
    fun fetchPlaces(@Query("types") types: String,
                    @Query("location") location: String,
                    @Query("key") key: String): Observable<PlaceResponse>

    @GET("maps/api/distancematrix/json?units=imperial")
    fun getDistances(@Query("origins") origins: String,
                    @Query("destinations") destinations: String,
                    @Query("key") key: String)  : Observable<DistanceResponse>
}
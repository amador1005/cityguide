package com.yuchen.cityguide.data.remote

import com.yuchen.cityguide.data.Place
import com.yuchen.cityguide.data.Response
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
                    @Query("key") key: String): Observable<Response>
}
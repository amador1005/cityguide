package com.yuchen.cityguide.data

import android.location.Location
import io.reactivex.Observable

/**
 * Created by yuchen on 1/5/18.
 */
interface PlacesDataSource {

    fun fetchPlaces(location: Location) : Observable<List<Place>>
}
package com.yuchen.cityguide.data.remote

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.tasks.Tasks
import com.yuchen.cityguide.Utilities
import com.yuchen.cityguide.data.PlacesDataSource
import io.reactivex.Observable
import io.reactivex.Observable.fromIterable
import java.util.concurrent.TimeUnit

/**
 * Created by yuchen on 1/5/18.
 */
class PlacesRemoteDataSource : PlacesDataSource {

    private val mGeoDataClient: GeoDataClient

    constructor(context: Context) {
        mGeoDataClient = Places.getGeoDataClient(context, null);

    }

    override fun fetchPlaces(location: Location): Observable<AutocompletePredictionBufferResponse> {
        return Observable.fromCallable {
            val task = mGeoDataClient.getAutocompletePredictions("restaurant", Utilities
                    .getLatLngBounds(location.latitude, location.longitude), null)
            Tasks.await(task, 60, TimeUnit.SECONDS)
            val predictions = task.result
            Log.i(TAG, "Query completed. Received " + predictions.getCount()
                    + " predictions.")
            predictions
        }
    }


    companion object {
        private val TAG = "PlacesRemoteDataSource"
    }
}
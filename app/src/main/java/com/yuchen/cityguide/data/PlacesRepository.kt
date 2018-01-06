package com.yuchen.cityguide.data

import android.location.Location
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse
import io.reactivex.Observable
import java.util.LinkedHashMap

/**
 * Created by yuchen on 1/5/18.
 */
object PlacesRepository {

    private var mPlacesRemoteDataSource: PlacesDataSource? = null

    internal var mCachedPlaces: MutableMap<Location, List<AutocompletePredictionBufferResponse>>? = null

    private var mCacheIsDirty = false

    fun registerDataSource(placesRemoteDataSource: PlacesDataSource) {
        mPlacesRemoteDataSource = checkNotNull(placesRemoteDataSource)
    }

    fun getPlaces(location: Location): Observable<AutocompletePredictionBufferResponse> {
        checkNotNull(mPlacesRemoteDataSource)
        checkNotNull(location)

        if (!mCacheIsDirty) {
            mCachedPlaces?.let {
                if (it.contains(location)) {
                    return Observable.fromIterable(it[location])
                }
            }
        }
        return getPlacesFromRemoteDataSource(location)
    }


    fun refreshPlaces() {
        mCacheIsDirty = true
    }

    private fun getPlacesFromRemoteDataSource(location: Location): Observable<AutocompletePredictionBufferResponse> {
        val predictions = mutableListOf<AutocompletePredictionBufferResponse>()
        return mPlacesRemoteDataSource!!.fetchPlaces(location)
                .doOnNext {
                    predictions.add(it)
                }
                .doOnComplete {
                    refreshCache(location, predictions)
                }
    }

    private fun refreshCache(location: Location, prediction: List<AutocompletePredictionBufferResponse>) {
        if (mCachedPlaces == null) {
            mCachedPlaces = LinkedHashMap()
        }
        mCachedPlaces!!.let {
            it.clear()
            it.put(location, prediction)
            mCacheIsDirty = false
        }
    }
}
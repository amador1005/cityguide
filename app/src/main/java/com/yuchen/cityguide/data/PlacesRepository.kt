package com.yuchen.cityguide.data

import android.location.Location
import android.util.Log
import io.reactivex.Observable
import java.util.LinkedHashMap

/**
 * Created by yuchen on 1/5/18.
 */
object PlacesRepository {

    private val TAG = "PlacesRepository"

    private var mPlacesRemoteDataSource: PlacesDataSource? = null

    internal var mCachedPlaces: MutableMap<Location, List<Place>>? = null

    private var mCacheIsDirty = false

    fun registerDataSource(placesRemoteDataSource: PlacesDataSource) {
        mPlacesRemoteDataSource = checkNotNull(placesRemoteDataSource)
    }

    fun getPlaces(location: Location): Observable<List<Place>> {
        checkNotNull(mPlacesRemoteDataSource)
        checkNotNull(location)

        if (!mCacheIsDirty) {
            mCachedPlaces?.let {
                if (it.contains(location)) {
                    Log.d(TAG, "cache hit")
                    return Observable.fromCallable { it[location] }
                }
            }
        }
        Log.d(TAG, "cache missed")
        return getPlacesFromRemoteDataSource(location)
    }


    fun refreshPlaces() {
        mCacheIsDirty = true
    }

    private fun getPlacesFromRemoteDataSource(location: Location): Observable<List<Place>> {
        return mPlacesRemoteDataSource!!.fetchPlaces(location)
                .doOnNext {
                    refreshCache(location, it.toList())
                }

    }

    private fun refreshCache(location: Location, prediction: List<Place>) {
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
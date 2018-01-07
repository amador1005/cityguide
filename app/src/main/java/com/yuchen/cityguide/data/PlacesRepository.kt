package com.yuchen.cityguide.data

import android.location.Location
import android.util.Log
import io.reactivex.Observable
import java.util.LinkedHashMap
import kotlin.math.log

/**
 * Created by yuchen on 1/5/18.
 */
object PlacesRepository {

    private val TAG = "PlacesRepository"

    private var mPlacesRemoteDataSource: PlacesDataSource? = null

    internal var mCachedPlaces: MutableMap<PlaceLocation, List<Place>>? = null

    private var mCacheIsDirty = false

    fun registerDataSource(placesRemoteDataSource: PlacesDataSource) {
        mPlacesRemoteDataSource = checkNotNull(placesRemoteDataSource)
    }

    fun getPlaces(location: PlaceLocation): Observable<List<Place>> {
        checkNotNull(mPlacesRemoteDataSource)
        checkNotNull(location)

        Log.d(TAG, "mCacheIsDirty $mCacheIsDirty, mCachedPlaces $mCachedPlaces ")

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

    private fun getPlacesFromRemoteDataSource(location: PlaceLocation): Observable<List<Place>> {
        return mPlacesRemoteDataSource!!.fetchPlaces(location)
                .doOnNext {
                    refreshCache(location, it.toList())
                }

    }

    private fun refreshCache(location: PlaceLocation, prediction: List<Place>) {
        if (mCachedPlaces == null) {
            mCachedPlaces = LinkedHashMap()
        }
        mCachedPlaces!!.let {
            it.clear()
            it.put(PlaceLocation(location), prediction)
            mCacheIsDirty = false
        }
    }

    class PlaceLocation(l: Location) : Location(l) {

        override fun equals(obj: Any?): Boolean {
            if (obj != null && obj is Location) {
                return longitude == obj.longitude && latitude == obj.latitude
            }
            return false
        }

        override fun hashCode(): Int {
            return (latitude.toString() + longitude.toString()).hashCode()
        }
    }
}


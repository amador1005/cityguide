package com.yuchen.cityguide

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Created by yuchen on 1/5/18.
 */

class Utilities {


    companion object {
        fun getLatLngBounds(latitude: Double, longitude: Double): LatLngBounds {
            val radiusDegrees = 1.0
            val northEast = LatLng(latitude + radiusDegrees, longitude + radiusDegrees)
            val southWest = LatLng(latitude - radiusDegrees, longitude - radiusDegrees)
            return LatLngBounds.builder()
                    .include(northEast)
                    .include(southWest)
                    .build();
        }

    }
}
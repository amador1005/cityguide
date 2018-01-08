package com.yuchen.cityguide.data

/**
 * Created by yuchen on 1/5/18.
 */

data class Place(var name: String, var geometry: Geometry, var rating: Float, var type:
PlaceType, var distance: String  = "")

data class Geometry(var location: Location)

data class Location(var lat: Double, var lng: Double)

data class PlaceResponse(var results: List<Place>)


enum class PlaceType private constructor() {
    BARS,
    CAFES,
    BISTROS
}
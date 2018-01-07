package com.yuchen.cityguide.ui

import android.databinding.BindingAdapter
import android.widget.ImageView
import android.widget.ListView
import com.yuchen.cityguide.R
import com.yuchen.cityguide.data.Place
import com.yuchen.cityguide.data.PlaceType

/**
 * Created by yuchen on 1/6/18.
 */
object PlacesItemBindings {
    @BindingAdapter("app:imageResource")
    @JvmStatic
    fun setImageResource(imageView: ImageView, place: Place) {
        when (place.type) {
            PlaceType.BARS -> imageView.setImageResource(R.drawable.ic_bar)
            PlaceType.CAFES -> imageView.setImageResource(R.drawable.ic_cafe)
            PlaceType.BISTROS -> imageView.setImageResource(R.drawable.ic_bistro)
        }
    }
}
package com.yuchen.cityguide.ui

import android.databinding.BindingAdapter
import android.widget.ListView
import com.yuchen.cityguide.data.Place

object PlacesListBindings {

    @BindingAdapter("app:items")
    @JvmStatic fun setItems(listView: ListView, items: List<Place>) {
        with(listView.adapter as PlacesAdapter) {
            replaceData(items)
        }
    }
}
package com.yuchen.cityguide.ui

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.yuchen.cityguide.data.Place
import com.yuchen.cityguide.databinding.PlaceItemBinding


/**
 * Created by yuchen on 1/6/18.
 */
class PlacesAdapter(
        private var places: List<Place>,
        private val placesViewModel: PlacesViewModel
) : BaseAdapter() {

    fun replaceData(places: List<Place>) {
        setList(places)
    }

    override fun getCount() = places.size

    override fun getItem(position: Int) = places[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        val binding: PlaceItemBinding
        if (view == null) {
            val inflater = LayoutInflater.from(viewGroup.context)
            binding = PlaceItemBinding.inflate(inflater, viewGroup, false)
        } else {
            binding = DataBindingUtil.getBinding<PlaceItemBinding>(view)
        }

        with(binding) {
            place = places[position]
            executePendingBindings()
        }

        return binding.root
    }


    private fun setList(places: List<Place>) {
        this.places = places
        notifyDataSetChanged()
    }
}
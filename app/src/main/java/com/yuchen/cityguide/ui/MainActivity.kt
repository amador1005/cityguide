package com.yuchen.cityguide.ui

import android.Manifest
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yuchen.cityguide.R
import com.yuchen.cityguide.data.PlacesRepository
import com.yuchen.cityguide.data.remote.PlacesRemoteDataSource
import com.yuchen.cityguide.databinding.ActivityMainBinding
import android.arch.lifecycle.ViewModelProviders


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewDataBinding: ActivityMainBinding
    private lateinit var listAdapter: PlacesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        PlacesRepository.registerDataSource(PlacesRemoteDataSource())
        val factory = PlacesViewModel.Factory(
                getApplication(), PlacesRepository)
        viewDataBinding.viewmodel = ViewModelProviders.of(this, factory).get(PlacesViewModel::class
                .java!!)
        setupListAdapter()
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST);

        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    location?.let {
                        Log.v(TAG, "location $location")
                        viewDataBinding.viewmodel?.loadPlaces(false, location)
                    }
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    Toast.makeText(this, "Location Permission is required for the app to work",
                            Toast
                                    .LENGTH_LONG)
                }
                return
            }
        }
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = PlacesAdapter(ArrayList(0), viewModel)
            viewDataBinding.placesList.adapter = listAdapter
        } else {
            Log.w(TAG, "ViewModel not initialized when attempting to set up adapter.")
        }
    }
/*
    private fun setupRefreshLayout() {
        viewDataBinding.refreshLayout.run {
            setColorSchemeColors(
                    ContextCompat.getColor(activity, R.color.colorPrimary),
                    ContextCompat.getColor(activity, R.color.colorAccent),
                    ContextCompat.getColor(activity, R.color.colorPrimaryDark)
            )
            // Set the scrolling view in the custom SwipeRefreshLayout.
            //scrollUpChild = viewDataBinding.tasksList
        }
    }
*/

    companion object {
        private val TAG = "MainActivity"
        private val PERMISSION_REQUEST = 12421;
    }
}

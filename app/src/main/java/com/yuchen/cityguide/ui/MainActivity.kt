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
import android.location.Location
import android.support.v4.widget.SwipeRefreshLayout


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewDataBinding: ActivityMainBinding
    private lateinit var listAdapter: PlacesAdapter
    private lateinit var viewModel: PlacesViewModel
    private var lastLocation: PlacesRepository.PlaceLocation? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupToolbar()
        setupTabs()
        setupRefreshLayout()

        PlacesRepository.registerDataSource(PlacesRemoteDataSource())
        val factory = PlacesViewModel.Factory(
                getApplication(), PlacesRepository)
        viewModel = ViewModelProviders.of(this, factory).get(PlacesViewModel::class
                .java!!)
        viewDataBinding.viewmodel = viewModel
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
                        lastLocation = PlacesRepository.PlaceLocation(it)
                        viewModel?.loadPlaces(false, false, lastLocation!!)
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

    private fun setupToolbar() {
        setSupportActionBar(viewDataBinding.toolbar)
        viewDataBinding.toolbar.setNavigationIcon(R.drawable.ic_menu)
    }

    private fun setupTabs() {
        viewDataBinding.tabs.run {
            addTab(this.newTab().setText(getText(R.string.label_bars)))
            addTab(this.newTab().setText(getText(R.string.label_bistros)))
            addTab(this.newTab().setText(getText(R.string.label_cafés)))
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

    private fun setupRefreshLayout() {
        val listener = this
        viewDataBinding.refreshLayout.run {
            setColorSchemeColors(
                    ContextCompat.getColor(context, R.color.red),
                    ContextCompat.getColor(context, R.color.blue),
                    ContextCompat.getColor(context, R.color.green),
                    ContextCompat.getColor(context, R.color.yellow)
            )
            scrollUpChild = viewDataBinding.placesList
            setOnRefreshListener(listener)
        }
    }

    override fun onRefresh() {
        Log.v(TAG, "onRefresh, lastLocation $lastLocation")
        if (lastLocation != null) {
            viewModel.loadPlaces(true, true, lastLocation!!)
        } else {
            viewModel.dataLoading.set(false)
        }
    }

    companion object {
        private val TAG = "MainActivity"
        private val PERMISSION_REQUEST = 12421;
    }
}

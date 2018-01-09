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
import android.support.v4.widget.SwipeRefreshLayout
import com.yuchen.cityguide.data.PlaceType
import com.yuchen.cityguide.view.SlidingMenu


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mViewDataBinding: ActivityMainBinding
    private lateinit var mListAdapter: PlacesAdapter
    private lateinit var mViewModel: PlacesViewModel
    private var mLastLocation: PlacesRepository.PlaceLocation? = null
    private var mLastShowLoadingUI = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupToolbar()
        setupSlidingMenu()
        setupRefreshLayout()
        setupmViewModel()
        setupmListAdapter()
    }

    override fun onResume() {
        super.onResume()
        getLocation(false)
    }

    private fun getLocation(showLoadingUI: Boolean) {
        Log.d(TAG, "getLocation")
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mLastShowLoadingUI = showLoadingUI
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST);

        } else {
            loadPlacesNearLocation(showLoadingUI)
        }
    }

    private fun loadPlacesNearLocation(showLoadingUI: Boolean) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    location?.let {
                        Log.v(TAG, "location $location")
                        mLastLocation = PlacesRepository.PlaceLocation(it)
                        mViewModel?.loadPlaces(showLoadingUI, showLoadingUI, mLastLocation!!)
                    }
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPlacesNearLocation(mLastShowLoadingUI)
                } else {
                    Toast.makeText(this, "Location Permission is required for the app to work",
                            Toast
                                    .LENGTH_LONG)
                }
                return
            }
        }
    }

    private fun setupmViewModel() {
        PlacesRepository.registerDataSource(PlacesRemoteDataSource())
        val factory = PlacesViewModel.Factory(
                getApplication(), PlacesRepository)
        mViewModel = ViewModelProviders.of(this, factory).get(PlacesViewModel::class
                .java!!)
        mViewDataBinding.viewmodel = mViewModel
    }

    private fun setupSlidingMenu() {
        val data = arrayOf(getString(R.string.label_bars), getString(R.string.label_bistros),
                getString(R.string.label_cafés))
        with(mViewDataBinding.slidingMenu) {
            setData(data)
            setSelect(0)
            setOnMenuSelectedListener(object : SlidingMenu.OnMenuSelectedListener {
                override fun onSelect(position: Int) {
                    mLastLocation?.let {
                        mViewModel.currentFiltering =
                                when (position) {
                                    0 -> PlaceType.BAR
                                    1 -> PlaceType.BISTRO
                                    else -> PlaceType.CAFE
                                }
                        mViewModel.loadPlaces(false, false, it)
                    }
                }
            })
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(mViewDataBinding.toolbar)
        mViewDataBinding.toolbar.setNavigationIcon(R.drawable.ic_menu)
    }

    private fun setupmListAdapter() {
        mListAdapter = PlacesAdapter(ArrayList(0), mViewModel)
        mViewDataBinding.placesList.adapter = mListAdapter
    }

    private fun setupRefreshLayout() {
        val listener = this
        mViewDataBinding.refreshLayout.run {
            setColorSchemeColors(
                    ContextCompat.getColor(context, R.color.red),
                    ContextCompat.getColor(context, R.color.blue),
                    ContextCompat.getColor(context, R.color.green),
                    ContextCompat.getColor(context, R.color.yellow)
            )
            scrollUpChild = mViewDataBinding.placesList
            setOnRefreshListener(listener)
        }
    }

    override fun onRefresh() {
        getLocation(true)
    }

    companion object {
        private val TAG = javaClass.simpleName
        private val PERMISSION_REQUEST = 12421;
    }
}

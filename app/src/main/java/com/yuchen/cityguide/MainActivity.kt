package com.yuchen.cityguide

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.yuchen.cityguide.data.PlacesRepository
import com.yuchen.cityguide.data.remote.PlacesRemoteDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.text.style.CharacterStyle




class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mGeoDataClient: GeoDataClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PlacesRepository.registerDataSource(PlacesRemoteDataSource())
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
        mGeoDataClient = Places.getGeoDataClient(this, null);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    location?.let {
                        // Logic to handle location object
                        Log.v(TAG, "location $location")
                        PlacesRepository.getPlaces(location)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(     // onNext
                                        { response ->
                                            response.forEach {
                                                Log.v(TAG, "name ${it.name}, type ${it.type}, " +
                                                        "rating ${it.rating}")

                                            }
                                            //processTasks(tasks)
                                            //mTasksView.setLoadingIndicator(false)
                                        },
                                        // onError
                                        { throwable -> run {
                                            throwable.stackTrace
                                            Log.v(TAG, "there is error " +
                                                "$throwable")} })



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
                    //todo
                }
                return
            }
        }
    }

    companion object {
        private val TAG = "MainActivity"
        private val PERMISSION_REQUEST = 12421;
    }
}

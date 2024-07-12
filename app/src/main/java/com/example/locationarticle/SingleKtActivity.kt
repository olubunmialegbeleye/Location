package com.example.locationarticle

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

class SingleKtActivity : AppCompatActivity() {

    private val mFusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationSettingsResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    getLastLocation()
                }

                Activity.RESULT_CANCELED -> {}
            }
        }

    override fun onStart() {
        super.onStart()
        setUpLocationComponentsAndGetLocation(this)
    }

    private fun setUpLocationComponentsAndGetLocation(activity: Activity) {
        if (isLocationPermissionGranted(activity)) {
            checkPhoneLocationSettings(activity) { isLocationSettingsOk ->
                if (isLocationSettingsOk) {
                    getLastLocation()
                } else {
                    //warn user
                }
            }
        } else {
            requestLocationPermission { permissionGranted ->
                if (permissionGranted) {
                    setUpLocationComponentsAndGetLocation(activity)
                } else {
                    //warn user
                }
            }
        }
    }

    private fun checkPhoneLocationSettings(
        activity: Activity,
        callback: (Boolean) -> Unit
    ) {

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val locationSettingsBuilder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result = LocationServices.getSettingsClient(activity)
            .checkLocationSettings(locationSettingsBuilder.build())

        result.addOnSuccessListener { callback(true) }

        result.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    locationSettingsResult.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun isLocationPermissionGranted(activity: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission(callback: (Boolean) -> Unit) {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted: Boolean =
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            val coarseLocationGranted: Boolean =
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

            //callback(fineLocationGranted || coarseLocationGranted)

            if (fineLocationGranted || coarseLocationGranted) {
                callback(true) //permission granted
            } else {
                callback(false) //permission denied
            }

        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Logic to handle location object
                Log.d("Location", "Long" + location.longitude)
                Log.d("Location", "Lat" + location.latitude)
            }
        }
    }
}
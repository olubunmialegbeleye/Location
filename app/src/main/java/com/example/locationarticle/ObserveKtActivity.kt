package com.example.locationarticle

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class ObserveKtActivity : AppCompatActivity() {

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .build()

    private val mFusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.longitude
            locationResult.lastLocation?.latitude

            // Logic to handle location object
            Log.d("Location", "Long" + locationResult.lastLocation?.longitude)
            Log.d("Location", "Lat" + locationResult.lastLocation?.latitude)
        }
    }

    private val locationSettingsResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    requestLocationUpdate(this@ObserveKtActivity)
                }

                Activity.RESULT_CANCELED -> {}
            }
        }

    override fun onStart() {
        super.onStart()
        setUpLocationComponentsAndObserveLocation(this, locationSettingsResult)
    }

    override fun onStop() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallBack)
        super.onStop()
    }

    private fun setUpLocationComponentsAndObserveLocation(
        activity: Activity,
        locationSettingsResult: ActivityResultLauncher<IntentSenderRequest>
    ) {
        if (isLocationPermissionGranted(activity)) {
            checkPhoneLocationSettings(
                activity,
                locationSettingsResult
            ) { isPhoneLocationSettingsOk ->
                if (isPhoneLocationSettingsOk) {
                    requestLocationUpdate(activity)
                } else {
                    //warn user
                }
            }
        } else {
            requestLocationPermission { permissionGranted ->
                if (permissionGranted) {
                    setUpLocationComponentsAndObserveLocation(activity, locationSettingsResult)
                } else {
                    //warn user
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate(activity: Activity) {
        if (isLocationPermissionGranted(activity)) {
            mFusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                Looper.getMainLooper()
            )
        }
    }

    private fun checkPhoneLocationSettings(
        activity: Activity,
        locationSettingsResult: ActivityResultLauncher<IntentSenderRequest>,
        callback: (Boolean) -> Unit
    ) {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { callback(true) }

        task.addOnFailureListener { exception ->
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
}
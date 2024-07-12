package com.example.locationarticle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

public class SingleJavaActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<IntentSenderRequest> locationSettingsResult;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setUpLocationSettingsResultCallback();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpLocationComponentsAndGetLocation(this);
    }

    /**
     * Must be called before onStart.
     * <p>
     * Sets up the {@link ActivityResultLauncher<IntentSenderRequest>} to handle the result of the Location Settings intent.
     * <p>
     * Registers an activity result callback using
     * {@link ActivityResultContracts.StartIntentSenderForResult}. The callback is invoked
     * after the user interacts with the location settings resolution activity.
     * <p>
     * If the user enables location settings (RESULT_OK), the method calls
     * {@link #getLastLocation()}.
     * <p>
     * If the user cancels or denies the request, the method behaves accordingly.
     */
    private void setUpLocationSettingsResultCallback() {
        locationSettingsResult = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                getLastLocation();
            } else {
                //Warn user that location is required for some features
            }
        });
    }


    private void setUpLocationComponentsAndGetLocation(Activity activity) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        if (isLocationPermissionGranted(activity)) {
            checkPhoneLocationSettings(activity, locationSettingsResult, isLocationSettingsOk -> {
                if (isLocationSettingsOk) {
                    getLastLocation();
                } else {
                    //You may decide to warn users that some functions may not be available without permission

                }
            });
        } else {
            requestLocationPermission(permissionGranted -> {
                if (permissionGranted) {
                    setUpLocationComponentsAndGetLocation(activity);
                } else {
                    //Warn user
                }
            });
        }
    }

    private boolean isLocationPermissionGranted(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                // Logic to handle location object
                Log.d("Location", "Long" + location.getLongitude());
                Log.d("Location", "Lat" + location.getLatitude());
            }
        });
    }

    private void requestLocationPermission(CallbackListener<Boolean> callbackListener) {

        ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (
                            fineLocationGranted != null && fineLocationGranted ||
                                    coarseLocationGranted != null && coarseLocationGranted
                    ) {
                        // Permission granted
                        callbackListener.onCallback(true);
                    } else {
                        // No location access granted.
                        callbackListener.onCallback(false);
                    }
                });

        locationPermissionRequest.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void checkPhoneLocationSettings(
            Activity activity,
            ActivityResultLauncher<IntentSenderRequest> locationSettingsResult,
            CallbackListener<Boolean> callbackListener) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(activity);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(activity, locationSettingsResponse -> {
            //Location settings is fine. You can start listening for location
            callbackListener.onCallback(true);
        });

        task.addOnFailureListener(activity, e -> {
            if (e instanceof ResolvableApiException) {
                locationSettingsResult.launch(new IntentSenderRequest.Builder(((ResolvableApiException) e).getResolution()).build());
            }
        });
    }
}

package com.example.locationarticle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

public class ObserveJavaActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<IntentSenderRequest> locationSettingsResult;
    private final LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .build();

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() != null) {
                Log.d("Location", "Long: " + locationResult.getLastLocation().getLongitude());
                Log.d("Location", "Lat: " + locationResult.getLastLocation().getLatitude());
            }
        }
    };


    public void onCreate(@Nullable Bundle savedInstanceState) {
        setUpLocationSettingsResultCallback();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpLocationComponentsAndObserveLocation(this);
    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    private void setUpLocationComponentsAndObserveLocation(Activity activity) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        if (isLocationPermissionGranted(activity)) {
            checkPhoneLocationSettings(activity, locationSettingsResult, isPhoneLocationSettingsOk -> {
                if (isPhoneLocationSettingsOk) {
                    requestLocationUpdate(activity);
                } else {
                    //warn user
                }
            });
        } else {
            requestLocationPermission(permissionGranted -> {
                if (permissionGranted) {
                    setUpLocationComponentsAndObserveLocation(activity);
                } else {
                    //warn user
                }
            });
        }
    }

    private void setUpLocationSettingsResultCallback() {
        locationSettingsResult = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                requestLocationUpdate(this);
            } else {
                //Warn user that location is required for some features
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdate(Activity activity) {
        if (isLocationPermissionGranted(activity)) {
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        }
    }

    private void checkPhoneLocationSettings(
            Activity activity,
            ActivityResultLauncher<IntentSenderRequest> locationSettingsResult,
            CallbackListener<Boolean> callbackListener) {

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

    private boolean isLocationPermissionGranted(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
}

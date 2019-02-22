package android.com.myapplication.loctiona_pck;

import android.Manifest;
import android.app.Activity;
import android.com.myapplication.R;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.os.Build.VERSION_CODES.M;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private final static int REQUEST_LOCATION_SETTING = 200;
    private static int LOCATION_REQUEST_CODE = 100;
    private TextView txtLocation, txt_loc;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;
    private double latitude = 0.0, longitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //get current location update callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        txtLocation.setText(String.format("Latituted=" + latitude + "\n Longituted=" + longitude));
                    }
                }
            }
        };


        intializeViews();

    }

    private void intializeViews() {
        txt_loc = findViewById(R.id.txt_loc);
        txtLocation = findViewById(R.id.txtLocation);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // get_Last_LocationUpdate();

        locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER).setInterval(2000);

        //user permission for location setting enable
        displayLocationSettingsRequest();

        //ask user permission for locationn access
        ask_User_RunTime_PerMission();
    }


    public void displayLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());


        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
                    }
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(MainActivity.this, REQUEST_LOCATION_SETTING);
                                break;
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }
            }
        });
    }


    private void ask_User_RunTime_PerMission() {
        if (Build.VERSION.SDK_INT >= M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_REQUEST_CODE);
            } else {
                // already permission granted
                get_Last_LocationUpdate();
            }
        } else {
            get_Last_LocationUpdate();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOCATION_SETTING: {
                if (resultCode == Activity.RESULT_OK) {
                    get_Last_LocationUpdate();
                } else {
                    Toast.makeText(this, "Location enable required !!!", Toast.LENGTH_SHORT).show();
                    txt_loc.setText("Location not available !!!");
                }
                break;
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("PerMission Granted", "----->>>>>");
                    // Toast.makeText(this, "PerMission Granted.......", Toast.LENGTH_SHORT).show();
                    get_Last_LocationUpdate();

                } else {
                    Log.e("PerMission Denied", "----->>>>>");
                    txt_loc.setText("Location not available !!!");
                    Toast.makeText(this, "Location permission granted required for latituted & longituted", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void get_Last_LocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                onStartLocationUpdate();
                txtLocation.setText(String.format("Latituted=" + latitude + "\n  Longituted=" + longitude));

            }

        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        onStopLocationUpdate();
    }

    @Override
    public void onStart() {
        super.onStart();
        onStartLocationUpdate();
    }

    private void onStartLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //permission required
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        onStartLocationUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void onStopLocationUpdate() {
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.e("Location", "");
    }
}

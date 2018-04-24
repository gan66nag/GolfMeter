package com.example.gan.golfmeter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Handler;
import android.renderscript.Double2;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final long LOCATION_TIME_INTERVAL = 8000;
    private static final int PERMISSION_CODE_GPS = 66;
    public boolean permissionGranted = false;
    TextView locStart;
    TextView locCurr;
    TextView distanceM;
    TextView distanceYd;
    TextView accuracyM;
    TextView accuracyYd;
    Button btnStartStop;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private Boolean setStart = true;
    private Location startLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locStart = (TextView) findViewById(R.id.locStart);
        locCurr = (TextView) findViewById(R.id.locCurr);
        distanceM = (TextView) findViewById(R.id.distanceM);
        distanceYd = (TextView) findViewById(R.id.distanceYd);
        accuracyM = (TextView) findViewById(R.id.accuracyM);
        accuracyYd = (TextView) findViewById(R.id.accuracyYd);
        btnStartStop = (Button) findViewById(R.id.btnStartStop);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_TIME_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_TIME_INTERVAL / 5);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocation = new Location("startLocation");
        if (savedInstanceState != null){
            startLocation.setLatitude(savedInstanceState.getDouble("latitude"));
            startLocation.setLongitude(savedInstanceState.getDouble("longitude"));
            startLocation.setAltitude(savedInstanceState.getDouble("altitude"));
            // Toast.makeText(getApplicationContext(), "Starting location restored.", Toast.LENGTH_SHORT).show();
            setStart = false;
        }

        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnCommand = (String) btnStartStop.getText();
                switch (btnCommand.toLowerCase()){
                    case "grant gps access":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_GPS);
                        } else {
                            permissionGranted = true;
                        }
                        break;
                    case "set start":
                        setStart = true;
                        btnStartStop.setClickable(false);
                        btnStartStop.setBackgroundColor(Color.YELLOW);
                    default:
                        break;
                }

            }
        });

        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        getSupportActionBar().setDisplayUseLogoEnabled(true);
//        getSupportActionBar().setLogo(R.drawable.distance_logo);
        getSupportActionBar().setIcon(R.drawable.distance_logo);
        getSupportActionBar().setTitle("GPS GolfMeter");
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdate();
    }

    private void requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_GPS);
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_GPS);
            } else {
                permissionGranted = true;
            }

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        btnStartStop.setText("Set Start");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "Connection suspended.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(), "Connection failed: %d", connectionResult.getErrorCode()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Float distanceMeters;
        Handler delayedActions = new Handler();

        locCurr.setBackgroundColor(Color.GREEN);

        if (setStart) {
            startLocation.set(location);
            setStart = false;
            btnStartStop.setClickable(true);
            btnStartStop.setBackgroundColor(Color.LTGRAY);
            locStart.setBackgroundColor(Color.GREEN);
        }

        delayedActions.postDelayed(new Runnable() {
            public void run() {
                locCurr.setBackgroundColor(Color.WHITE);
                locStart.setBackgroundColor(Color.WHITE);
            }
        }, LOCATION_TIME_INTERVAL/10);

        locStart.setText(String.format(Locale.getDefault(),
                "lat:%.5f lon:%.5f alt:%.0fm (+/- %.0fm)",
                startLocation.getLatitude(),
                startLocation.getLongitude(),
                startLocation.getAltitude(),
                startLocation.getAccuracy())
        );
        locCurr.setText(String.format(Locale.getDefault(),
                "lat:%.5f lon:%.5f alt:%.0fm (+/- %.0fm)",
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getAccuracy())
        );

        distanceMeters = location.distanceTo(startLocation);
        distanceM.setText(String.format(Locale.getDefault(), "%.0f m", distanceMeters));
        accuracyM.setText(String.format(Locale.getDefault(), "+/- %.0f", location.getAccuracy()));
        distanceYd.setText(String.format(Locale.getDefault(), "%.0f yds", distanceMeters*1.0936));
        accuracyYd.setText(String.format(Locale.getDefault(), "+/- %.0f", location.getAccuracy()*1.0936));
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(googleApiClient.isConnected())
            requestLocationUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            //Toast.makeText(getApplicationContext(), "Location updates removed. Application onPause().", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
            //Toast.makeText(getApplicationContext(), "Google services stopped. Application onStop().", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putDouble("latitude", startLocation.getLatitude());
        outState.putDouble("longitude", startLocation.getLongitude());
        outState.putDouble("altitude", startLocation.getAltitude());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE_GPS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    permissionGranted = true;
                    btnStartStop.setText("Set Start");
                } else {
                    permissionGranted = false;
                    Toast.makeText(getApplicationContext(), "This app requires access to GPS location.", Toast.LENGTH_SHORT).show();
                    distanceM.setText("no GPS access");
                    distanceYd.setText("no GPS access");
                    btnStartStop.setText("Grant GPS access");
                }
                break;
            default:
                Toast.makeText(getApplicationContext(), "We shouldn't be here!!! Permission result switch case on <default:>", Toast.LENGTH_LONG).show();
        }
    }
}

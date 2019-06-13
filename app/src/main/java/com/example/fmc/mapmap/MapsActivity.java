package com.example.fmc.mapmap;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.RoundCap;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    Marker currentpos;
    private String TAG = "showAdminPath";
    String info;
    //Define list to get all latlng for the route
    List<LatLng> path = new ArrayList();

    int geofence = 0;
    int start = 0;
    private long UPDATE_INTERVAL = 10000;  /* 10 secs */
    private long FASTEST_INTERVAL = 10000; /* 10 sec */
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    GoogleApiClient gac;
    LocationRequest locationRequest;
    Button btn, getRoutebtn;
    EditText textId;
    int check = 0;
    int working = 0;
    RelativeLayout relativelayout;
    BottomSheetBehavior bottomSheetBehavior;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        relativelayout = (RelativeLayout)findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(relativelayout);

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        textId = (EditText) findViewById(R.id.editText);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        textId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textId.setText("");
            }
        });
        getRoutebtn = (Button) findViewById(R.id.button2);
        btn = (Button) findViewById(R.id.getlastlocation);
        btn.setVisibility(View.GONE);
        btn.setEnabled(false);

        textId.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textId.getApplicationWindowToken(), 0);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    geofence=1;
                    getAdminRoute();
                    btn.setVisibility(View.VISIBLE);
                    btn.setEnabled(true);
                    check=0;
                    btn.setBackgroundResource(R.drawable.play);
                    return true;
                }
                return false;
            }
        });
        // Capture button clicks
        /**************GET ROUTE BUTTON***************************/
        getRoutebtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Button b = (Button)v;
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                geofence=1;
                getAdminRoute();
                btn.setVisibility(View.VISIBLE);
                check=0;
                btn.setBackgroundResource(R.drawable.play);
                btn.setEnabled(true);
            }
        });

        /**************START BUTTON***************************/
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Button b = (Button)v;
                String buttonText = b.getText().toString();
                if(check==0){
                    b.setBackgroundResource(R.drawable.stop);
                    check = 1;
                }
                else{
                    b.setBackgroundResource(R.drawable.play);
                    check = 0;
                    working=0;
                    textId.setText("");
                    mMap.clear();
                    path.clear();
                }
            }
        });

        isGooglePlayServicesAvailable();

        if(!isLocationEnabled())
            showAlert();

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gac = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    protected void onStart() {
        gac.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            updateUI(location);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        Log.d(TAG, "onConnected");

        Location ll = LocationServices.FusedLocationApi.getLastLocation(gac);
        Log.d(TAG, "LastLocation: " + (ll == null ? "NO LastLocation" : ll.toString()));

        LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MapsActivity.this, "Permission was granted!", Toast.LENGTH_LONG).show();

                    try{
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                gac, locationRequest, this);
                    } catch (SecurityException e) {
                        Toast.makeText(MapsActivity.this, "SecurityException:\n" + e.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MapsActivity.this, "onConnectionFailed: \n" + connectionResult.toString(),
                Toast.LENGTH_LONG).show();
        Log.d("DDD", connectionResult.toString());
    }

    /*************************************MAIN WORKING******************************************/
    private void updateUI(Location loc) {
        Log.d(TAG, "updateUI");

        /****************working....When route is received************/
        if(working==1) {
            Log.d(TAG, "In working");
            int close_count = 0; //route points close to current location
            for (int i = 0; i < path.size(); i++) {
                float results[] = new float[10];
                Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), path.get(i).latitude, path.get(i).longitude, results);
                if (results[0] <= 300) {
                    close_count = close_count + 1;
                }
                else{
                }
            }
            if(close_count>0){geofence=1;}
            else{geofence=0;}
            if (geofence == 0) {
                //Send notification
                final Intent emptyIntent = new Intent();
                PendingIntent pendingIntent = PendingIntent.getActivity(MapsActivity.this, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                                .setContentTitle("Straying From Path")
                                .setContentText("You are moving away. Please stay on the path drawn.")
                                .setContentIntent(pendingIntent);
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(123, mBuilder.build());
            }
            //Send location information to server
            if (check == 1) {
                String strayed;
                if(geofence==0){
                    strayed = "1";
                }
                else{
                    strayed = "0";
                }
                AndroidNetworking.post("http://b.com/sanaIntern/add_loc.php")
                        .addBodyParameter("devId", "sana")
                        .addBodyParameter("lat", Double.toString(loc.getLatitude()))
                        .addBodyParameter("lng", Double.toString(loc.getLongitude()))
                        .addBodyParameter("timeloc", DateFormat.getTimeInstance().format(loc.getTime()))
                        .addBodyParameter("routeId", textId.getText().toString())
                        .addBodyParameter("strayed",strayed)
                        .setTag("test")
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                            }

                            @Override
                            public void onError(ANError error) {
                                if (error.getErrorCode() != 0) {
                                    Log.d(TAG, "onError errorCode : " + error.getErrorCode());
                                    Log.d(TAG, "onError errorBody : " + error.getErrorBody());
                                    Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                                } else {
                                    // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                                    Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                                }
                            }
                        });


            } else {
            }
        }
        //make current position marker
        if(currentpos!=null) {
            currentpos.remove();
        }
        currentpos = mMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(),loc.getLongitude())).title("Your Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cast_ic_stop_circle_filled_grey600)));

        LatLngBounds.Builder builder2 = new LatLngBounds.Builder();
        builder2.include(new LatLng(loc.getLatitude(),loc.getLongitude()));
        //initialize the padding for map boundary
        int padding = 10;
        //create the bounds from latlngBuilder to set into map camera
        LatLngBounds bounds = builder2.build();

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        mMap.setPadding(50, 50, 50, 120);
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 16.0f ) );
    }
    /*******************************************************************************************/

    private boolean isLocationEnabled() {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGooglePlayServicesAvailable() {
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.d(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        Log.d(TAG, "This device is supported.");
        return true;
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void setWorking(int work){
        working = work;
    }

    /********************************GET ADMIN ROUTE************************************/
    public void getAdminRoute(){
        mMap.clear();
        path.clear();

        AndroidNetworking.post("http://b.com/sanaIntern/get_admin_path.php")
                .addBodyParameter("routeId",textId.getText().toString())
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Data received");
                        try {
                            Log.d(TAG, "State is: "+response.getString("status"));
                            if(response.getDouble("status")==1){
                                JSONArray rows= response.getJSONArray("result");
                                for(int i=0;i<rows.length(); i++) {
                                    JSONObject latlng = rows.getJSONObject(i);
                                    Log.d(TAG, "double values:" + latlng.getDouble("longitude"));
                                    path.add(new LatLng(latlng.getDouble("latitude"), latlng.getDouble("longitude")));
                                    //builder.include(new LatLng(latlng.getDouble("latitude"), latlng.getDouble("longitude")));

                                }
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(path.get(0));
                                builder.include(path.get(path.size()-1));
                                //Draw the polyline
                                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.RED).width(7 * 1).geodesic(true)
                                        .startCap(new RoundCap()).endCap(new RoundCap());
                                Polyline poly =  mMap.addPolyline(opts);
                                mMap.addMarker(new MarkerOptions().position(path.get(0)).title("Starting")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                                mMap.addMarker(new MarkerOptions().position(path.get(path.size()-1)).title("Ending")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                //initialize the padding for map boundary
                                int padding = 50;
                                //create the bounds from latlngBuilder to set into map camera
                                LatLngBounds bounds = builder.build();

                                mMap.getUiSettings().setZoomControlsEnabled(true);

                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                                setWorking(1);
                            }
                            else{
                                Toast.makeText(MapsActivity.this, "This route does not exist.", Toast.LENGTH_LONG).show();
                                setWorking(0);
                            }

                        } catch (JSONException e) {

                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        if (error.getErrorCode() != 0) {
                            // received error from server
                            // error.getErrorCode() - the error code from server, error.getErrorBody() - the error body from server,
                            // error.getErrorDetail() - just an error detail
                            Log.d(TAG, "onError errorCode : " + error.getErrorCode());
                            Log.d(TAG, "onError errorBody : " + error.getErrorBody());
                            Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                        }
                    }
                });
    }
}
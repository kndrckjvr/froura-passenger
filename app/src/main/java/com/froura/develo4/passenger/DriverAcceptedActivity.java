package com.froura.develo4.passenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.froura.develo4.passenger.libraries.DialogCreator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.grantland.widget.AutofitHelper;

public class DriverAcceptedActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        DialogCreator.DialogActionListener {

    private GoogleMap mMap;
    private CameraPosition cameraPosition;
    private Marker driverMarker;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private TextView name;
    private CircleImageView prof_pic;
    private LinearLayout informationLayout;

    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_accepted);

        toolbar = findViewById(R.id.toolbar_driver_accepted);
        toolbar.setTitle("Driver on the way");
        setSupportActionBar(toolbar);
        informationLayout = findViewById(R.id.information_layout);

        driverId = getIntent().getStringExtra("driverId");

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        View v = navigationView.getHeaderView(0);
        name = v.findViewById(R.id.txtVw_name);
        prof_pic = v.findViewById(R.id.imgVw_profile_pic);
        AutofitHelper.create(name);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        showDriverLocation();
    }

    private void showDriverLocation() {
        DatabaseReference driverLoc = FirebaseDatabase.getInstance().getReference("available_drivers/" + driverId + "/l");
        driverLoc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Object> map = (List<Object>) dataSnapshot.getValue();
                double drvLat = 0;
                double drvLng = 0;

                if(dataSnapshot.getValue() != null) {
                    if(map.get(0) != null){
                        drvLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        drvLng = Double.parseDouble(map.get(1).toString());
                    }
                }

                LatLng drvLatLng = new LatLng(drvLat, drvLng);

                if(driverMarker != null) {
                    driverMarker.remove();
                }

                Location drvLoc = new Location("");
                drvLoc.setLatitude(drvLat);
                drvLoc.setLongitude(drvLng);

                Location pickupLoc = new Location("");
                pickupLoc.setLatitude(getIntent().getDoubleExtra("pickupLat", 0));
                pickupLoc.setLongitude(getIntent().getDoubleExtra("pickupLng", 0));

                float dist = pickupLoc.distanceTo(drvLoc);
                if(dist <= 100) {
                    Toast.makeText(DriverAcceptedActivity.this, "Your Driver has arrived", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DriverAcceptedActivity.this, "Your driver is approximately " + dist + " meters", Toast.LENGTH_SHORT).show();
                }

                driverMarker = mMap.addMarker(new MarkerOptions().position(drvLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext() != null) {
            mLastLocation = location;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

            if (!success) {
                Log.e("Booking", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Booking", "Can't find style. Error: ", e);
        }
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setPadding(0,0,0,informationLayout.getLayoutParams().height);
        buildGoogleApiClient();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) { }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            DialogCreator.create(this, "locationPermission")
                    .setMessage("We need to access your location and device state to continue using FROURÁ.")
                    .setPositiveButton("OK")
                    .show();
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onClickPositiveButton(String actionId) {
        switch (actionId) {
            case "":
                ActivityCompat.requestPermissions(DriverAcceptedActivity
                                .this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                break;
        }
    }

    @Override
    public void onClickNegativeButton(String actionId) {

    }

    @Override
    public void onClickNeutralButton(String actionId) { }

    @Override
    public void onClickMultiChoiceItem(String actionId, int which, boolean isChecked) { }

    @Override
    public void onCreateDialogView(String actionId, View view) { }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
}

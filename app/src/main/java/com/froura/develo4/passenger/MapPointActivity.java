package com.froura.develo4.passenger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.froura.develo4.passenger.libraries.DialogCreator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MapPointActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GeoDataClient mGeoDataClient;

    private boolean hasPickup = false;
    private boolean hasDropoff = false;
    private int from;
    private String pickupPlaceId = "";
    private String dropoffPlaceId = "";
    private LatLng pickupLatLng;
    private LatLng dropoffLatLng;
    private String pickupName;
    private String dropoffName;
    private Marker pickupMarker;
    private Marker dropoffMarker;

    private Button set_button;
    private ImageButton my_location_button;
    private ImageButton zoom_out_button;
    private CardView point_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_point);

        mGeoDataClient = Places.getGeoDataClient(this, null);
        from = getIntent().getIntExtra("from", -1);
        if(getIntent().getStringExtra("pickupPlaceId") != null) {
            hasPickup = true;
            pickupPlaceId = getIntent().getStringExtra("pickupPlaceId");
        }
        if(getIntent().getStringExtra("dropoffPlaceId") != null) {
            hasDropoff = true;
            dropoffPlaceId = getIntent().getStringExtra("dropoffPlaceId");
        }

        set_button = findViewById(R.id.set_button);
        zoom_out_button = findViewById(R.id.zoom_out_button);
        my_location_button = findViewById(R.id.my_location_button);
        point_layout = findViewById(R.id.point_layout);
        set_button.setText(from == 0 ? "SET AS PICKUP POINT" : "SET AS DROP-OFF POINT");

        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        zoom_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMarkers();
            }
        });

        my_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPosition cameraPosition;
                if(mLastLocation != null)
                    cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .zoom(14)
                            .bearing(0)
                            .build();
                else
                    cameraPosition = new CameraPosition.Builder()
                            .target(pickupLatLng)
                            .zoom(14)
                            .bearing(0)
                            .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
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
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));
        } catch (Resources.NotFoundException e) { }
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setPadding(0,0, 0, point_layout.getLayoutParams().height);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                switch (from) {
                    case 0:
                        if(pickupMarker != null) {
                            pickupMarker.remove();
                        }
                        pickupMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
                        break;
                    case 1:
                        if(dropoffMarker != null) {
                            dropoffMarker.remove();
                        }
                        dropoffMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.black_marker)));
                        break;
                }
                findPlaceByLatLng(latLng);
            }
        });
        if(hasPickup) {
            findPlaceById(pickupPlaceId, "pickup");
        }
        if(hasDropoff) {
            findPlaceById(dropoffPlaceId, "dropoff");
        }
        buildGoogleApiClient();
    }

    private void findPlaceByLatLng(LatLng latLng) {
        Geocoder geoCoder = new Geocoder(this);
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) { }
        Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
        if(bestMatch != null) {

        }
    }

    private void findPlaceById(String placeId, final String marker) {
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    switch (marker) {
                        case "pickup":
                            pickupLatLng = myPlace.getLatLng();
                            pickupName = myPlace.getName().toString();
                            break;
                        case "dropoff":
                            dropoffLatLng = myPlace.getLatLng();
                            dropoffName = myPlace.getName().toString();
                            break;
                    }
                    setMarkers();
                    places.release();
                }
            }
        });
    }

    private void setMarkers() {
        if(pickupMarker != null && dropoffMarker != null) {
            mMap.clear();
        }
        if(pickupLatLng != null) {
            pickupMarker = mMap.addMarker(new MarkerOptions()
                    .position(pickupLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
        }

        if(dropoffLatLng != null) {
            dropoffMarker = mMap.addMarker(new MarkerOptions()
                    .position(dropoffLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.black_marker)));
        }

        if(dropoffLatLng != null && pickupLatLng != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickupLatLng);
            builder.include(dropoffLatLng);
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
        } else {
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition.Builder()
                            .target(pickupLatLng)
                            .zoom(14)
                            .bearing(0)
                            .build()));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MapPointActivity.this, LandingActivity.class);
        if(hasPickup) {
            intent.putExtra("hasPickup", 1);
            intent.putExtra("pickupPlaceId", pickupPlaceId);
        }
        if(hasDropoff) {
            intent.putExtra("hasDropoff", 1);
            intent.putExtra("dropoffPlaceId", dropoffPlaceId);
        }
        startActivity(intent);
        finish();
    }
}

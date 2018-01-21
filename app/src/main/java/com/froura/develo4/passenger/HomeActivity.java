package com.froura.develo4.passenger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.froura.develo4.passenger.libraries.DialogCreator;
import com.froura.develo4.passenger.libraries.SnackBarCreator;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity
        implements DialogCreator.DialogActionListener,
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private DrawerLayout drawer;
    private TextView name;
    private FloatingActionButton bookFab;
    private FloatingActionButton rsrvFab;
    private View viewFab;
    private CardView viewDetails;
    private CircleImageView prof_pic;

    private GoogleMap mMap;
    private CameraPosition cameraPosition;
    private SupportMapFragment mapFragment;

    private PlaceAutocompleteFragment pickup;
    private PlaceAutocompleteFragment dropoff;

    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient mGoogleSignInApiClient;
    private Location mLastLocation;
    private LatLng pickupLocation;
    private LatLng dropoffLocation;

    private String uid;
    private String pickupName;
    private String dropoffName;
    private boolean autoMarkerSet = false;
    private boolean cameraUpdated = false;
    final int LOCATION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        Glide.with(this)
                .load(getImage("placeholder"))
                .into(prof_pic);

        bookFab = findViewById(R.id.bookFab);
        rsrvFab = findViewById(R.id.rsrvFab);
        viewFab = findViewById(R.id.viewFab);
        viewDetails = findViewById(R.id.details);
        viewDetails.animate()
                .translationY(viewDetails.getHeight())
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        viewDetails.setVisibility(View.GONE);
                    }
                });

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(!locationEnabled())
            DialogCreator.create(this, "requestLocation")
                    .setTitle("Access Location")
                    .setMessage("Turn on your location settings to be able to get location data.")
                    .setPositiveButton("Go to Settings")
                    .show();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("PH")
                .build();

        pickup = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.pickup);
        dropoff = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.dropoff);

        pickup.setFilter(typeFilter);
        dropoff.setFilter(typeFilter);

        pickup.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                pickupName = place.getName().toString();
                pickupLocation = place.getLatLng();
                setMarkers();
            }

            @Override
            public void onError(Status status) { }
        });
        dropoff.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                dropoffName = place.getName().toString();
                dropoffLocation = place.getLatLng();
                setMarkers();
            }

            @Override
            public void onError(Status status) { }
        });

        pickup.getView().findViewById(R.id.clear)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setVisibility(View.GONE);
                        pickupLocation = null;
                        autoMarkerSet = false;
                        setMarkers();
                    }
                });
        dropoff.getView().findViewById(R.id.clear)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setVisibility(View.GONE);
                        dropoff.setText("");
                        dropoffLocation = null;
                        dropoffName = null;
                        setMarkers();
                    }
                });

        bookFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareBooking();
            }
        });
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
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null){
            mLastLocation = location;
            if(!autoMarkerSet)
                setMarkers();
            if(!cameraUpdated) {
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(14)
                        .bearing(0)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                cameraUpdated = true;
            }

            if(getIntent().getIntExtra("bookAct", -1) == 1) {
                pickup.setText(getIntent().getStringExtra("pickupLoc"));
                dropoff.setText(getIntent().getStringExtra("dropoffLoc"));
                pickupLocation = new LatLng(getIntent().getDoubleExtra("pickupLat", 0.0), getIntent().getDoubleExtra("pickupLng", 0.0) );
                autoMarkerSet = true;
                setMarkers();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        buildGoogleApiClient();
    }

    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.logout:
                String providerid = "";
                for(UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                    providerid = user.getProviderId();
                }
                if(AccessToken.getCurrentAccessToken() != null && providerid.equals("facebook.com")) {
                    LoginManager.getInstance().logOut();
                } else if(providerid.equals("google.com")) {
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();

                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                    mGoogleSignInClient.signOut();
                }
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    SnackBarCreator.set("Permissions denied.");
                    SnackBarCreator.show(viewFab);
                    permissionDenied();
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onClickPositiveButton(String actionId) {
        switch (actionId) {
            case "requestLocation":
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onClickNegativeButton(String actionId) { }

    @Override
    public void onClickNeutralButton(String actionId) { }

    @Override
    public void onClickMultiChoiceItem(String actionId, int which, boolean isChecked) { }

    @Override
    public void onCreateDialogView(String actionId, View view) { }

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources()
                .getIdentifier(imageName, "drawable", this.getPackageName());

        return drawableResourceId;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void setMarkers() {
        mMap.clear();
        if(autoMarkerSet) {
            if(pickupLocation != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(pickupLocation.latitude, pickupLocation.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
            } else if(mLastLocation != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
            }
            if(dropoffLocation != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(dropoffLocation.latitude, dropoffLocation.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.black_marker)));
            }
        } else {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
            showCurrentPlace();
            autoMarkerSet = true;
        }
    }

    private boolean locationEnabled() {
        int locationMode = 0;
        String locationProviders;
        boolean isAvailable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }

        return isAvailable;
    }

    private void askPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            Toast.makeText(this, "All Permissions granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void permissionDenied() {
        LatLng latLng = new LatLng(14.6091, 121.0223);
        cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(11)
                .bearing(0)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        bookFab.setImageResource(R.mipmap.book_disabled);
        rsrvFab.setImageResource(R.mipmap.rsrv_disabled);

        bookFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SnackBarCreator.set("Permissions denied.");
                SnackBarCreator.show(view);
            }
        });

        rsrvFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SnackBarCreator.set("Permissions denied.");
                SnackBarCreator.show(view);
            }
        });
    }

    private boolean permissionStatus() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean detailsComplete = true;
    private void prepareBooking() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child("passenger").child(uid);
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if(map.get("email").equals("null") && map.get("mobnum").equals("null")) {
                        SnackBarCreator.set("Please set your email and mobile number before booking.");
                    } else if(map.get("mobnum").equals("null")) {
                        SnackBarCreator.set("Please set your mobile number before booking.");
                    } else if(map.get("email").equals("null")) {
                        SnackBarCreator.set("Please set your email before booking.");
                    }

                    if(map.get("email").equals("null") || map.get("mobnum").equals("null")) {
                        SnackBarCreator.show(viewFab);
                        detailsComplete = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        if(permissionStatus() && detailsComplete) {
            if(pickupName != null && dropoffName != null) {
                Intent intent = new Intent(this, BookingActivity.class);
                intent.putExtra("user_id", uid);
                if(pickupLocation != null) {
                    intent.putExtra("pickupLat", pickupLocation.latitude);
                    intent.putExtra("pickupLng", pickupLocation.longitude);
                } else {
                    intent.putExtra("pickupLat", mLastLocation.getLatitude());
                    intent.putExtra("pickupLng", mLastLocation.getLongitude());
                }
                intent.putExtra("pickupName", pickupName);
                intent.putExtra("dropoffName", dropoffName);
                startActivity(intent);
                finish();
            } else {
                SnackBarCreator.set("Set a Drop-off point.");
                SnackBarCreator.show(viewFab);
            }
        } else {
            SnackBarCreator.set("Permissions denied.");
            SnackBarCreator.show(viewFab);
        }
    }

    private void showCurrentPlace() {
        if (locationEnabled()) {
            PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
            @SuppressWarnings("MissingPermission")
            final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null && pickup != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    pickupName = (String) placeLikelihood.getPlace().getName();
                                    pickupLocation = placeLikelihood.getPlace().getLatLng();
                                    pickup.setText(pickupName);
                                }
                                likelyPlaces.release();
                            }
                        }
                    });
        }
    }
}

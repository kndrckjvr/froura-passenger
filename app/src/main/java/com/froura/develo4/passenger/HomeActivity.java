package com.froura.develo4.passenger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.util.Log;
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
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
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

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.grantland.widget.AutofitHelper;

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
    private TextView pickupTxtVw;
    private TextView dropoffTxtVw;

    private GoogleMap mMap;
    private CameraPosition cameraPosition;
    private SupportMapFragment mapFragment;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng pickupLocation;
    private LatLng dropoffLocation;

    private String uid;
    private String pickupName;
    private String dropoffName;
    private String pickupPlaceID;
    private String dropoffPlaceID;
    private boolean autoMarkerSet = false;
    private boolean cameraUpdated = false;
    final int LOCATION_REQUEST_CODE = 1;

    //User Details
    private String user_name;
    private String user_mobnum;
    private String user_email;

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
        AutofitHelper.create(name);
        Glide.with(this)
                .load(getImage("placeholder"))
                .into(prof_pic);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setDetails();

        bookFab = findViewById(R.id.bookFab);
        rsrvFab = findViewById(R.id.rsrvFab);
        viewFab = findViewById(R.id.viewFab);
        viewDetails = findViewById(R.id.details);
        pickupTxtVw = findViewById(R.id.txtVw_pickup);
        dropoffTxtVw = findViewById(R.id.txtVw_dropoff);


        if(!locationEnabled())
            DialogCreator.create(this, "requestLocation")
                    .setTitle("Access Location")
                    .setMessage("Turn on your location settings to be able to get location data.")
                    .setPositiveButton("Go to Settings")
                    .show();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        pickupTxtVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        bookFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareBooking();
            }
        });

        prof_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDetails();
            }
        });
    }

    private void setDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String JSON_DETAILS_KEY = "userDetails";
        String userDetails = sharedPref.getString(JSON_DETAILS_KEY, "{ \"name\" : NULL }");
        Toast.makeText(this, userDetails, Toast.LENGTH_SHORT).show();
        try {
            JSONObject jsonObject = new JSONObject(userDetails);

            Toast.makeText(this, jsonObject.getString("name"), Toast.LENGTH_SHORT).show();
            if(!jsonObject.getString("name").equals("NULL")) {
                name.setText(jsonObject.getString("name"));
                Glide.with(this)
                        .load(jsonObject.getString("profile_pic"))
                        .into(prof_pic);
                user_name = jsonObject.getString("name");
                user_email = jsonObject.getString("email");
                user_mobnum = jsonObject.getString("mobnum");
            }
        } catch (Exception e) {
            Log.e("details", "setDetails: ", e);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
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
        if(!autoMarkerSet) {
            PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
            final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    pickupName = placeLikelihood.getPlace().getName().toString();
                                    pickupLocation = placeLikelihood.getPlace().getLatLng();
                                    pickupPlaceID = placeLikelihood.getPlace().getId();
                                    setText(pickupTxtVw, pickupName);
                                    mMap.clear();
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(pickupLocation.latitude, pickupLocation.longitude))
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.yellow_marker)));
                                    autoMarkerSet = true;
                                }
                                likelyPlaces.release();
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null){
            mLastLocation = location;

            if(!cameraUpdated) {
                cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(),location.getLongitude()))
                        .zoom(14)
                        .bearing(0)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                cameraUpdated = true;
            }
        }
    }

    private void setText(TextView txtVw, String str) {
        txtVw.setText(str);
        txtVw.setTextColor(getResources().getColor(R.color.place_autocomplete_search_text));
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
                permissionDenied(viewFab);
            }
        });

        rsrvFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionDenied(viewFab);
            }
        });
    }

    private boolean permissionStatus() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    private void prepareBooking() {
        if(permissionStatus()) {
            if(checkDetails() == 1) {
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
            } else if(checkDetails() == -1) {
                SnackBarCreator.set("Please set your Mobile Number.");
                SnackBarCreator.show(viewFab);
            } else if(checkDetails() == 2) {
                SnackBarCreator.set("Please set your Email Address.");
                SnackBarCreator.show(viewFab);
            } else {
                SnackBarCreator.set("Please set your Mobile Number and Email Address.");
                SnackBarCreator.show(viewFab);
            }
        } else {
            permissionDenied(viewFab);
        }
    }

    private int checkDetails() {
        if(user_mobnum.equals("null") && user_email.equals("null")) {
            return  -3;
        }
        if(user_mobnum.equals("null")) {
            return -1;
        }
        if(user_email.equals("null")) {
            return -2;
        }
        return 1;
    }

    private void setHint(TextView txtVw, String str) {
        txtVw.setText(str);
        txtVw.setTextColor(getResources().getColor(R.color.place_autocomplete_search_hint));
    }

    private void permissionDenied(View view) {
        SnackBarCreator.set("Permissions denied.");
        SnackBarCreator.show(viewFab);
    }
}

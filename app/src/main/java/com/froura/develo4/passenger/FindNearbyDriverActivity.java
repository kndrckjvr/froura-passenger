package com.froura.develo4.passenger;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FindNearbyDriverActivity extends AppCompatActivity {

    private CountDownTimer timer;
    private TextView cntDwnTxtVw;
    private String uid;
    private DatabaseReference bookingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_nearby_driver);

        uid = FirebaseAuth.getInstance().getUid();
        cntDwnTxtVw = findViewById(R.id.cntDwnTxtVw);
        bookingRef = FirebaseDatabase.getInstance().getReference().child("services").child("booking").child(uid);
        timer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long l) {
                cntDwnTxtVw.setText("CANCEL IN " + l / 1000);
            }

            @Override
            public void onFinish() {
                sendBooking();
            }
        };
        timer.start();
    }

    private void sendBooking() {
        bookingRef.child("pickupName").setValue(getIntent().getStringExtra("pickupName"));
        bookingRef.child("pickupLocation").child("0").setValue(getIntent().getDoubleExtra("pickupLat", 0));
        bookingRef.child("pickupLocation").child("1").setValue(getIntent().getDoubleExtra("pickupLng", 0));
        bookingRef.child("dropoffName").setValue(getIntent().getStringExtra("dropoffName"));
        bookingRef.child("dropoffLocation").child("0").setValue(getIntent().getDoubleExtra("dropoffLat", 0));
        bookingRef.child("dropoffLocation").child("1").setValue(getIntent().getDoubleExtra("dropoffLng", 0));
        bookingRef.child("fare").setValue(getIntent().getStringExtra("fare"));
        findNearbyDriver();
    }

    private int radius = 1;
    private GeoQuery geoQuery;
    private boolean driverFound = false;
    private void findNearbyDriver() {
        DatabaseReference drvAvailable = FirebaseDatabase.getInstance().getReference().child("available_drivers");

        GeoFire geoFire = new GeoFire(drvAvailable);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(getIntent().getDoubleExtra("pickupLat", 0), getIntent().getDoubleExtra("pickupLng", 0)), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(driverFound) return;
                bookingRef.child("nearest_driver").setValue(key);
                driverFound = true;
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                radius++;
                findNearbyDriver();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        bookingRef.removeValue();
        Intent intent = new Intent(FindNearbyDriverActivity.this, BookingActivity.class);
        intent.putExtra("hasPickup", 1);
        intent.putExtra("pickupPlaceId", getIntent().getStringExtra("pickupPlaceId"));
        intent.putExtra("hasDropoff", 1);
        intent.putExtra("dropoffPlaceId", getIntent().getStringExtra("dropoffPlaceId"));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //bookingRef.removeValue();
    }
}

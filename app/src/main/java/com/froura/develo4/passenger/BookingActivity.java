package com.froura.develo4.passenger;

import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.froura.develo4.passenger.libraries.SnackBarCreator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BookingActivity extends AppCompatActivity {

    private Double pickupLat;
    private Double pickupLng;
    private String pickupLoc;
    private String dropoffLoc;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        pickupLat = getIntent().getDoubleExtra("pickupLat", 0.0);
        pickupLng = getIntent().getDoubleExtra("pickupLng", 0.0);
        pickupLoc = getIntent().getStringExtra("pickupLoc");
        dropoffLoc = getIntent().getStringExtra("dropoffLoc");

        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                setBookingDetails();
            }
        };
    }

    private void setBookingDetails() {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("services")
                .child("booking");
        GeoFire geoFire = new GeoFire(dbRef);
        geoFire.setLocation(user_id, new GeoLocation(pickupLat, pickupLng));
        dbRef.child("pickup").setValue(pickupLoc);
        dbRef.child("dropoff").setValue(dropoffLoc);
    }
}

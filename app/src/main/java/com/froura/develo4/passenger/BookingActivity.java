package com.froura.develo4.passenger;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private TextView timertxt;
    private Button cancel;

    private Double pickupLat;
    private Double pickupLng;
    private String pickupName;
    private String dropoffName;
    private String uid;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        pickupLat = getIntent().getDoubleExtra("pickupLat", 0.0);
        pickupLng = getIntent().getDoubleExtra("pickupLng", 0.0);
        pickupName = getIntent().getStringExtra("pickupName");
        dropoffName = getIntent().getStringExtra("dropoffName");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        timertxt = findViewById(R.id.textView4);
        cancel = findViewById(R.id.button2);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                returnHome();
            }
        });

        timer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long l) {
                timertxt.setText("CANCEL IN "+ l / 1000);
            }

            @Override
            public void onFinish() {
                timertxt.setVisibility(TextView.GONE);
                cancel.setVisibility(TextView.GONE);
                setBookingDetails();
            }
        };
        timer.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnHome();
    }

    private void returnHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("pickupName", pickupName);
        intent.putExtra("dropoffName", dropoffName);
        intent.putExtra("pickupLat", pickupLat);
        intent.putExtra("pickupLng", pickupLng);
        intent.putExtra("bookAct", 1);
        finish();
        startActivity(intent);
    }

    private void setBookingDetails() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("services").child("booking");
        GeoFire geoFire = new GeoFire(dbRef);
        geoFire.setLocation(uid, new GeoLocation(pickupLat, pickupLng));
        dbRef.child(uid).child("pickup").setValue(pickupName);
        dbRef.child(uid).child("dropoff").setValue(dropoffName);
        getClosestDriver();
    }

    private GeoQuery geoQuery;
    private int radius = 1;
    private boolean driverFound = false;

    private void getClosestDriver() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance()
                .getReference().child("working_drivers");
        final DatabaseReference jobRef = FirebaseDatabase.getInstance()
                .getReference().child("services").child("booking").child(uid);

        GeoFire geoFire = new GeoFire(driverRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLat, pickupLng), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                jobRef.child("nearbyDriver").setValue(key);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}

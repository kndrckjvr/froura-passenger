package com.froura.develo4.passenger;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.froura.develo4.passenger.config.TaskConfig;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;

public class ReservationActivity extends AppCompatActivity implements SuperTask.TaskListener {

    private String reservationAddress;
    TextView response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        response = findViewById(R.id.textView4);
        response.setMovementMethod(new ScrollingMovementMethod());
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        reservationAddress = getIntent().getStringExtra("destinationName") != null ?
                getIntent().getStringExtra("destinationName") :
                getIntent().getStringExtra("destinationAddress");
        Log.d(TaskConfig.TAG, reservationAddress);
        SuperTask.execute(this, TaskConfig.CHECK_TARIFF_URL, "check", "Loading data...");
    }

    @Override
    public void onTaskRespond(String json, String id) {
        response.setText(json+"");
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("place", reservationAddress);
        return contentValues;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ReservationActivity.this, LandingActivity.class);
        if(getIntent().getStringExtra("destinationPlaceId") != null) {
            intent.putExtra("destinationPlaceId", getIntent().getStringExtra("destinationPlaceId"));
        }

        if(getIntent().getStringExtra("destinationName") != null) {
            intent.putExtra("destinationName", getIntent().getStringExtra("destinationName"));
            intent.putExtra("destinationLatLng", getIntent().getStringExtra("destinationLatLng"));
            intent.putExtra("destinationLat", getIntent().getDoubleExtra("destinationLat", 0));
            intent.putExtra("destinationLng", getIntent().getDoubleExtra("destinationLng", 0));

        }
        intent.putExtra("hasDestination", 1);
        startActivity(intent);
        finish();
    }
}

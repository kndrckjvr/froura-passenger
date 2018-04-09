package com.froura.develo4.passenger.reservation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.froura.develo4.passenger.R;
import com.froura.develo4.passenger.libraries.SimpleDividerItemLine;
import com.froura.develo4.passenger.object.PlaceAutocompleteObject;
import com.froura.develo4.passenger.adapter.PlaceAutocompleteAdapter;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

public class TerminalActivity extends AppCompatActivity implements PlaceAutocompleteAdapter.PlaceAutoCompleteInterface {

    private RecyclerView listRecVw;
    private PlaceAutocompleteAdapter mAdapter;
    private GeoDataClient mGeoDataClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listRecVw = findViewById(R.id.terminal_rec_vw);
        listRecVw.setHasFixedSize(true);
        listRecVw.setLayoutManager(new LinearLayoutManager(this));
        listRecVw.addItemDecoration(new SimpleDividerItemLine(this));
        mGeoDataClient = Places.getGeoDataClient(this, null);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("PH")
                .build();

        mAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, typeFilter, true);
        listRecVw.setAdapter(mAdapter);
        mAdapter.getFilter().filter("NAIA TERMINAL");
    }

    @Override
    public void onPlaceClick(ArrayList<PlaceAutocompleteObject> mResultList, int position) {
        Intent intent = new Intent(TerminalActivity.this, DateTimeNoteActivity.class);
        intent.putExtra("isPickup", getIntent().getBooleanExtra("isPickup", true));
        intent.putExtra("destinationPlaceId", getIntent().getStringExtra("destinationPlaceId"));
        intent.putExtra("destinationName", getIntent().getStringExtra("destinationName"));
        intent.putExtra("destinationLat", getIntent().getDoubleExtra("destinationLat", 0));
        intent.putExtra("destinationLng", getIntent().getDoubleExtra("destinationLng", 0));
        intent.putExtra("fare", getIntent().getStringExtra("fare"));
        intent.putExtra("terminalId", mResultList.get(position).getPlaceId());
        startActivity(intent);
        finish();
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
        Intent intent = new Intent(TerminalActivity.this, TarrifCheckActivity.class);
        if(getIntent().getStringExtra("destinationPlaceId") != null) {
            intent.putExtra("destinationPlaceId", getIntent().getStringExtra("destinationPlaceId"));
        } else {
            intent.putExtra("destinationName", getIntent().getStringExtra("destinationName"));
            intent.putExtra("destinationLat", getIntent().getDoubleExtra("destinationLat", 0));
            intent.putExtra("destinationLng", getIntent().getDoubleExtra("destinationLng", 0));
        }
        intent.putExtra("isPickup", getIntent().getBooleanExtra("isPickup", true));
        intent.putExtra("cityPos", getIntent().getIntExtra("isPickup", -1));
        intent.putExtra("townPos", getIntent().getIntExtra("isPickup", -1));
        startActivity(intent);
        finish();
    }
}

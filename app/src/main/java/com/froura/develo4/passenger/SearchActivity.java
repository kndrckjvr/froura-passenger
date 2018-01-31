package com.froura.develo4.passenger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.froura.develo4.passenger.adapter.PlaceAutocompleteAdapter;
import com.froura.develo4.passenger.object.PlaceAutocompleteObject;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements PlaceAutocompleteAdapter.PlaceAutoCompleteInterface {

    private EditText searchET;
    private ImageView clearImgVw;
    private RecyclerView listRecVw;
    private PlaceAutocompleteAdapter mAdapter;

    private GeoDataClient mGeoDataClient;
    private String pickupPlaceId;
    private String dropoffPlaceId;
    private int hasPickup = -1;
    private int hasDropoff = -1;
    private int from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if(getIntent().getStringExtra("pickupPlaceId") != null) {
            hasPickup = 1;
            pickupPlaceId = getIntent().getStringExtra("pickupPlaceId");
        }

        if(getIntent().getStringExtra("dropoffPlaceId") != null){
            hasDropoff = 1;
            dropoffPlaceId = getIntent().getStringExtra("dropoffPlaceId");
        }

        from = getIntent().getIntExtra("from", -1);

        searchET = findViewById(R.id.searchET);
        clearImgVw = findViewById(R.id.clearImgVw);
        listRecVw = findViewById(R.id.listRecVw);
        listRecVw.setHasFixedSize(true);
        listRecVw.setLayoutManager(new LinearLayoutManager(this));

        mGeoDataClient = Places.getGeoDataClient(this, null);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("PH")
                .build();

        mAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, typeFilter);
        listRecVw.setAdapter(mAdapter);

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()) {
                    mAdapter.getFilter().filter(charSequence);
                    clearImgVw.setVisibility(View.VISIBLE);
                } else {
                    clearImgVw.setVisibility(View.GONE);
                    if(mAdapter != null) mAdapter.clearList();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        clearImgVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAdapter != null) mAdapter.clearList();
            }
        });
    }

    @Override
    public void onPlaceClick(ArrayList<PlaceAutocompleteObject> mResultList, int position) {
        Intent intent = new Intent(SearchActivity.this, HomeActivity.class);
        if(from == 0) {
            if(hasPickup == 1) {
                intent.putExtra("hasPickup", 1);
                intent.putExtra("pickupPlaceId", mResultList.get(position).getPlaceId());
            }

            if(hasDropoff == 1) {
                intent.putExtra("hasDropoff", 1);
                intent.putExtra("dropoffPlaceId", dropoffPlaceId);
            }
        } else {
            if(hasPickup == 1) {
                intent.putExtra("hasPickup", 1);
                intent.putExtra("pickupPlaceId", pickupPlaceId);
            }

            if(hasDropoff == 1) {
                intent.putExtra("hasDropoff", 1);
                intent.putExtra("dropoffPlaceId", mResultList.get(position).getPlaceId());
            } else {
                intent.putExtra("hasDropoff", 1);
                intent.putExtra("dropoffPlaceId", mResultList.get(position).getPlaceId());
            }
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SearchActivity.this, HomeActivity.class);
        if(hasPickup == 1) {
            intent.putExtra("hasPickup", 1);
            intent.putExtra("pickupPlaceId", pickupPlaceId);
        }
        if(hasDropoff == 1) {
            intent.putExtra("hasDropoff", 1);
            intent.putExtra("dropoffPlaceId", dropoffPlaceId);
        }
        startActivity(intent);
        finish();
    }
}

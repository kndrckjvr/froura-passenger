package com.froura.develo4.passenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.froura.develo4.passenger.adapter.PlaceAutocompleteAdapter;
import com.froura.develo4.passenger.object.PlaceAutocompleteObject;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements PlaceAutocompleteAdapter.PlaceAutoCompleteInterface {

    private EditText searchET;
    private ImageButton clearImgVw;
    private TextView emptyView;
    private RecyclerView listRecVw;
    private PlaceAutocompleteAdapter mAdapter;

    private GeoDataClient mGeoDataClient;
    private String pickupPlaceId;
    private String dropoffPlaceId;
    private int hasPickup = -1;
    private int hasDropoff = -1;
    private int from;
    private CharSequence text;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

//        Toolbar myToolbar = findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);

        if(getIntent().getStringExtra("pickupPlaceId") != null) {
            hasPickup = 1;
            pickupPlaceId = getIntent().getStringExtra("pickupPlaceId");
        }

        if(getIntent().getStringExtra("dropoffPlaceId") != null){
            hasDropoff = 1;
            dropoffPlaceId = getIntent().getStringExtra("dropoffPlaceId");
        }

        searchET = findViewById(R.id.searchET);
        clearImgVw = findViewById(R.id.clearImgVw);
        listRecVw = findViewById(R.id.listRecVw);
        emptyView = findViewById(R.id.listEmpty);
        listRecVw.setHasFixedSize(true);
        listRecVw.setLayoutManager(new LinearLayoutManager(this));

        from = getIntent().getIntExtra("from", -1);

        timer = new CountDownTimer(2000, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                mAdapter.getFilter().filter(text);
            }
        };


        if(from == 0) {
            searchET.setHint("Enter Pick-up point.");
        } else {
            searchET.setHint("Where are you going?");
        }

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
                text = charSequence;
                timer.cancel();

                if(!text.toString().isEmpty()) {
                    clearImgVw.setVisibility(View.VISIBLE);
                } else {
                    clearImgVw.setVisibility(View.GONE);
                    if(mAdapter != null) mAdapter.clearList();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                timer.start();
            }
        });

        clearImgVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAdapter != null)
                    mAdapter.clearList();
                searchET.setText("");
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

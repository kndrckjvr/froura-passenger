package com.froura.develo4.passenger.reservation;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.froura.develo4.passenger.LandingActivity;
import com.froura.develo4.passenger.R;
import com.froura.develo4.passenger.config.TaskConfig;
import com.froura.develo4.passenger.libraries.SnackBarCreator;
import com.froura.develo4.passenger.object.CityObject;
import com.froura.develo4.passenger.object.StateObject;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import me.grantland.widget.AutofitHelper;

public class TarrifCheckActivity extends AppCompatActivity implements SuperTask.TaskListener {

    private String destinationName;
    private ArrayList<StateObject> state_list = new ArrayList<>();
    private ArrayList<CityObject> city_list = new ArrayList<>();
    private ArrayList<String> state_name = new ArrayList<>();
    private ArrayList<String> city_name = new ArrayList<>();
    private GeoDataClient mGeoDataClient;
    private LatLng destinationLatLng;
    private ProgressDialog progressDialog;

    private TextView city_txt_vw;
    private TextView town_txt_vw;
    private TextView price_txt_vw;
    private Spinner town_spinner;
    private Spinner city_spinner;
    private RadioButton pickup_rbtn;
    private RadioButton dropoff_rbtn;
    private Button proceed_btn;
    private ArrayAdapter<String> town_adapter;
    private ArrayAdapter<String> city_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tariff_check);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Details...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        mGeoDataClient = Places.getGeoDataClient(this, null);

        city_txt_vw = findViewById(R.id.city_txt_vw);
        town_txt_vw = findViewById(R.id.town_txt_vw);
        price_txt_vw = findViewById(R.id.price_txt_vw);
        pickup_rbtn = findViewById(R.id.pickup_rbtn);
        dropoff_rbtn = findViewById(R.id.dropoff_rbtn);
        proceed_btn = findViewById(R.id.proceed_btn);
        AutofitHelper.create(town_txt_vw);

        if(getIntent().getStringExtra("destinationPlaceId") != null) {
            findPlaceById(getIntent().getStringExtra("destinationPlaceId"));
        } else {
            destinationName = getIntent().getStringExtra("destinationName");
            destinationLatLng = new LatLng(getIntent().getDoubleExtra("destinationLat", 0.0),
                    getIntent().getDoubleExtra("destinationLng", 0.0));
            SuperTask.execute(TarrifCheckActivity.this,
                    TaskConfig.CHECK_TARIFF_URL, "get_tariff");
        }

        proceed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceed();
            }
        });
    }

    private void findPlaceById(String placeId) {
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    destinationName = myPlace.getAddress().toString();
                    destinationLatLng = myPlace.getLatLng();
                    places.release();

                    SuperTask.execute(TarrifCheckActivity.this,
                            TaskConfig.CHECK_TARIFF_URL, "get_tariff");
                }
            }
        });
    }

    private void proceed() {
        Intent intent = new Intent(TarrifCheckActivity.this, TerminalActivity.class);
        intent.putExtra("isPickup", pickup_rbtn.isChecked());
        intent.putExtra("fare", price_txt_vw.getText().toString().substring(4));
        intent.putExtra("destinationPlaceId", getIntent().getStringExtra("destinationPlaceId"));
        intent.putExtra("destinationName", destinationName);
        intent.putExtra("destinationLat", destinationLatLng.latitude);
        intent.putExtra("destinationLng", destinationLatLng.longitude);
        startActivity(intent);
        finish();
    }

    @Override
    public void onTaskRespond(String json, String id) {
        try {
            switch (id) {
                case "get_tariff":
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray stateArray = jsonObject.getJSONArray("state");
                    for(int i = 0; i < stateArray.length(); i++) {
                        JSONObject stateObjects = stateArray.getJSONObject(i);
                        state_name.add(stateObjects.getString("name"));
                        state_list.add(new StateObject(stateObjects.getInt("id"),
                                stateObjects.getString("name")));
                    }

                    JSONArray cityArray = jsonObject.getJSONArray("city");
                    for(int i = 0; i < cityArray.length(); i++) {
                        JSONObject cityObjects = cityArray.getJSONObject(i);
                        city_name.add(cityObjects.getString("name"));
                        city_list.add(new CityObject(cityObjects.getInt("id"),
                                cityObjects.getString("name"),
                                cityObjects.getInt("state_id"),
                                cityObjects.getString("price")));
                    }
                    town_spinner = findViewById(R.id.town_spinner);
                    city_spinner = findViewById(R.id.city_spinner);
                    town_adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, city_name);
                    town_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    town_spinner.setAdapter(town_adapter);
                    town_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            updatePrice(i);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) { }
                    });
                    city_adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, state_name);
                    city_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    city_spinner.setAdapter(city_adapter);
                    city_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            updateTown(i + 1);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) { }
                    });

                    boolean stateFound = true;
                    boolean cityFound = true;

                    if(stateArray.length() > 1) {
                        city_txt_vw.setText("");
                        city_spinner.setVisibility(View.VISIBLE);
                        stateFound = false;
                    } else {
                        city_txt_vw.setText(state_list.get(0).getName());
                    }

                    if(cityArray.length() > 1) {
                        town_txt_vw.setText("");
                        town_spinner.setVisibility(View.VISIBLE);
                        price_txt_vw.setText("Php " + city_list.get(0).getPrice());
                        cityFound = false;
                    } else {
                        town_txt_vw.setText(city_list.get(0).getName());
                        price_txt_vw.setText("Php " + city_list.get(0).getPrice());
                    }
                    progressDialog.dismiss();
                    if(!stateFound && !cityFound) {
                        SnackBarCreator.set("City and District/Town not found.");
                        SnackBarCreator.show(proceed_btn);
                    } else if(!cityFound) {
                        SnackBarCreator.set("District/Town not found.");
                        SnackBarCreator.show(proceed_btn);
                    }
                    break;
            }
        } catch (Exception e) { Log.e(TaskConfig.TAG, e.getMessage()); }
    }

    private void updateTown(int i) {
        city_name.clear();
        for(CityObject cityObject : city_list)
            if(cityObject.getState_id() == i)
                city_name.add(cityObject.getName());

        town_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, city_name);
        town_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        town_spinner.setAdapter(town_adapter);
        town_adapter.notifyDataSetChanged();
        updatePrice(0);
    }

    private void updatePrice(int i) {
        for(CityObject cityObject : city_list)
            if(cityObject.getName() == city_name.get(i)) {
                price_txt_vw.setText("Php " + cityObject.getPrice());
                break;
            }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("place", destinationName);
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
        Intent intent = new Intent(TarrifCheckActivity.this, LandingActivity.class);
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

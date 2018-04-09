package com.froura.develo4.passenger.reservation;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.froura.develo4.passenger.LandingActivity;
import com.froura.develo4.passenger.R;
import com.froura.develo4.passenger.adapter.ReservationListAdapter;
import com.froura.develo4.passenger.config.TaskConfig;
import com.froura.develo4.passenger.libraries.SimpleDividerItemSpace;
import com.froura.develo4.passenger.object.ReservationObject;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReservationListActivity extends AppCompatActivity implements SuperTask.TaskListener,
        ReservationListAdapter.ReservationListAdapterListener {

    private ArrayList<ReservationObject> mReservationList = new ArrayList<>();
    private RecyclerView res_list_rec_vw;
    private ReservationListAdapter resAdapter;
    private RelativeLayout loading_view;
    private RelativeLayout blank_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        res_list_rec_vw = findViewById(R.id.res_list_rec_vw);
        resAdapter = new ReservationListAdapter(this, this, mReservationList);
        res_list_rec_vw.setAdapter(resAdapter);
        res_list_rec_vw.setHasFixedSize(true);
        res_list_rec_vw.setLayoutManager(new LinearLayoutManager(this));
        loading_view = findViewById(R.id.loading_view);
        blank_view = findViewById(R.id.blank_view);
        loading_view.setVisibility(View.VISIBLE);
        SuperTask.execute(this,
                TaskConfig.RESERVATION_LIST_URL,
                "");
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
        Intent intent = new Intent(ReservationListActivity.this, LandingActivity.class);
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

    @Override
    public void onReservationListClick(ArrayList<ReservationObject> resultList, int position) {

    }

    @Override
    public void onTaskRespond(String json, String id) {
        mReservationList.clear();
        Log.d(TaskConfig.TAG, ""+json);
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.getBoolean("success")) {
                JSONArray jsonArray = jsonObject.getJSONArray("reservations");
                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject reservation = jsonArray.getJSONObject(i);
                    mReservationList.add(new ReservationObject(reservation.getInt("id"),
                            reservation.getString("driver_id"),
                            reservation.getString("start_destination"),
                            reservation.getString("end_destination"),
                            new LatLng(reservation.getDouble("start_lat"), reservation.getDouble("start_lng")),
                            new LatLng(reservation.getDouble("end_lat"), reservation.getDouble("end_lng")),
                            reservation.getString("start_id"),
                            reservation.getString("end_id"),
                            reservation.getString("reservation_date"),
                            reservation.getString("price"),
                            reservation.getString("notes"),
                            reservation.getInt("status")));
                }
                loading_view.setVisibility(View.GONE);
                res_list_rec_vw.setVisibility(View.VISIBLE);
                resAdapter.notifyDataSetChanged();
            } else  {
                loading_view.setVisibility(View.GONE);
                blank_view.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) { }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("type", "passenger");
        contentValues.put("id", FirebaseAuth.getInstance().getUid());
        return contentValues;
    }
}

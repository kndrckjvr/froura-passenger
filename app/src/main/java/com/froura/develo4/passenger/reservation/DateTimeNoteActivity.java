package com.froura.develo4.passenger.reservation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.froura.develo4.passenger.LandingActivity;
import com.froura.develo4.passenger.R;
import com.froura.develo4.passenger.config.TaskConfig;
import com.froura.develo4.passenger.libraries.SnackBarCreator;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class DateTimeNoteActivity extends AppCompatActivity implements SuperTask.TaskListener {

    private Button change_date_btn;
    private Button change_time_btn;
    private Button proceed_btn;
    private TextView date_txt_vw;
    private TextView time_txt_vw;
    private EditText note_et;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private Calendar cal;
    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private int currentHour;
    private int currentMinute;
    private GeoDataClient mGeoDataClient;

    private String reservationDate;
    private String reservationTime;
    private String user_database_id;
    private String terminalName;
    private String[] monthNames = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private double terminalLat = 0.0;
    private double terminalLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time_note);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mGeoDataClient = Places.getGeoDataClient(this, null);
        cal = Calendar.getInstance();
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);
        currentDay = cal.get(Calendar.DAY_OF_MONTH);
        currentHour = cal.get(Calendar.HOUR_OF_DAY);
        currentMinute = cal.get(Calendar.MINUTE);
        note_et = findViewById(R.id.note_et);
        change_date_btn = findViewById(R.id.change_date_btn);
        change_time_btn = findViewById(R.id.change_time_btn);
        proceed_btn = findViewById(R.id.proceed_btn);
        date_txt_vw = findViewById(R.id.date_txt_vw);
        time_txt_vw = findViewById(R.id.time_txt_vw);
        date_txt_vw.setText(String.format(Locale.ENGLISH, "%s %02d, %04d",
        monthNames[currentMonth], (currentDay + 1), currentYear));
        time_txt_vw.setText(String.format(Locale.ENGLISH, "%02d:%02d %s",
                (currentHour == 12 || currentHour == 0) ? 12 : currentHour % 12,
                currentMinute, (currentHour >= 12) ? "PM" : "AM"));

        reservationDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d",
                currentYear, (currentMonth + 1), (currentDay + 1));
        reservationTime = String.format(Locale.ENGLISH, "%02d:%02d:00",
                currentHour, currentMinute);

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                if(year < currentYear || month < currentMonth || day <= currentDay) {
                    SnackBarCreator.set("Reservation Dates must be a day after the current date.");
                    SnackBarCreator.show(proceed_btn);
                } else {
                    date_txt_vw.setText(String.format(Locale.ENGLISH, "%s %02d, %04d",
                            monthNames[month], day, year));
                    month += 1;
                    reservationDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d",
                            year, month, day);
                }
            }
        };

        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                time_txt_vw.setText(String.format(Locale.ENGLISH, "%02d:%02d %s",
                        (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12,
                        minute, (hourOfDay >= 12) ? "PM" : "AM"));
                reservationTime = String.format(Locale.ENGLISH, "%02d:%02d:00",
                        hourOfDay, minute);
            }
        };

        change_date_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(DateTimeNoteActivity.this,
                        mDateSetListener, currentYear, currentMonth, currentDay + 1);
                dialog.show();
            }
        });

        change_time_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog dialog = new TimePickerDialog(
                        DateTimeNoteActivity.this, mTimeSetListener, currentHour, currentMinute, false);
                dialog.show();
            }
        });

        proceed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuperTask.execute(DateTimeNoteActivity.this,
                        TaskConfig.RESERVATION_URL,
                        "reserve",
                        "Processing Reservation...");
            }
        });

        setDetails();
        findPlaceById(getIntent().getStringExtra("terminalId"));
    }

    private void findPlaceById(String placeId) {
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    terminalName = myPlace.getName().toString();
                    terminalLat = myPlace.getLatLng().latitude;
                    terminalLng = myPlace.getLatLng().longitude;
                    places.release();
                }
            }
        });
    }

    private void setDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String JSON_DETAILS_KEY = "userDetails";
        String userDetails = sharedPref.getString(JSON_DETAILS_KEY, "{ \"name\" : NULL }");
        try {
            JSONObject jsonObject = new JSONObject(userDetails);
            if (!jsonObject.getString("name").equals("NULL")) {
                user_database_id = jsonObject.getString("database_id");
            }
        } catch (Exception e) { }
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
        Intent intent = new Intent(DateTimeNoteActivity.this, TerminalActivity.class);
        if(getIntent().getStringExtra("destinationPlaceId") != null)
            intent.putExtra("destinationPlaceId", getIntent().getStringExtra("destinationPlaceId"));
        intent.putExtra("destinationName", getIntent().getStringExtra("destinationName"));
        intent.putExtra("destinationLat", getIntent().getDoubleExtra("destinationLat", 0));
        intent.putExtra("destinationLng", getIntent().getDoubleExtra("destinationLng", 0));
        intent.putExtra("fare", getIntent().getStringExtra("fare"));
        startActivity(intent);
        finish();
    }

    @Override
    public void onTaskRespond(String json, String id) {
        Log.d(TaskConfig.TAG, json+"");
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.getBoolean("success")) {
                Intent intent = new Intent(DateTimeNoteActivity.this, LandingActivity.class);
                intent.putExtra("reservationComplete", true);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) { }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("android", 1);
        contentValues.put("userid", FirebaseAuth.getInstance().getUid());
        contentValues.put("database_id", user_database_id);
        contentValues.put("date", reservationDate);
        contentValues.put("time", reservationTime);
        contentValues.put("notes", note_et.getText().toString().isEmpty() ? "" : note_et.getText().toString());
        contentValues.put("start_id", getIntent().getStringExtra("terminalId"));
        contentValues.put("start_point", terminalName);
        contentValues.put("start_lat", terminalLat);
        contentValues.put("start_lng", terminalLng);
        contentValues.put("type_point", getIntent().getBooleanExtra("isPickup", true) ? 0 : 1);
        if(getIntent().getStringExtra("destinationPlaceId") != null) {
            contentValues.put("end_point", getIntent().getStringExtra("destinationName"));
            contentValues.put("end_id", getIntent().getStringExtra("destinationPlaceId"));
        } else {
            contentValues.put("end_point", getIntent().getStringExtra("destinationName"));
            contentValues.put("end_id", "");
        }
        contentValues.put("end_lat", getIntent().getDoubleExtra("destinationLat", 0.0));
        contentValues.put("end_lng", getIntent().getDoubleExtra("destinationLng", 0.0));
        contentValues.put("fare", getIntent().getStringExtra("fare"));
        contentValues.put("origin_state", 0);
        contentValues.put("origin_city", 0);
        Log.d(TaskConfig.TAG, "setReq: "+contentValues);
        return contentValues;
    }
}

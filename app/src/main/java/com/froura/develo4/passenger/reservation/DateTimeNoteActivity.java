package com.froura.develo4.passenger.reservation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.froura.develo4.passenger.R;
import com.froura.develo4.passenger.config.TaskConfig;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

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
    private double terminalLat = 0.0;
    private double terminalLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time_note);

        mGeoDataClient = Places.getGeoDataClient(this, null);
        cal = Calendar.getInstance();
        currentYear = currentYear = cal.get(Calendar.YEAR);
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
        date_txt_vw.setText(reservationDate = String.format(Locale.ENGLISH, "%02d/%02d/%04d",
                (currentMonth + 1), (currentDay + 1), currentYear));
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
                    Toast.makeText(DateTimeNoteActivity.this,
                            "Reservation Dates must be a day after the current date.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    month += 1;
                    reservationDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d",
                            year, month, day);
                    date_txt_vw.setText(String.format(Locale.ENGLISH, "%02d/%02d/%04d",
                            month, day, year));
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
                        "Sending Reservation...");
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
    public void onTaskRespond(String json, String id) {
        Log.d(TaskConfig.TAG, "datetime: "+json);
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
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
        contentValues.put("origin_state", 1);
        contentValues.put("origin_city", 1);
        Log.d(TaskConfig.TAG, "setReq: "+contentValues);
        return contentValues;
    }
}

package com.froura.develo4.passenger.reservation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.froura.develo4.passenger.R;
import com.froura.develo4.passenger.config.TaskConfig;

import java.util.Calendar;

public class DateTimeNoteActivity extends AppCompatActivity {

    private Button change_date_btn;
    private Button change_time_btn;
    private TextView date_txt_vw;
    private TextView time_txt_vw;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private Calendar cal;
    private int currentYear;
    private int currentMonth;
    private int currentDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time_note);

        cal = Calendar.getInstance();
        currentYear = currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);
        currentDay = cal.get(Calendar.DAY_OF_MONTH);
        change_date_btn = findViewById(R.id.change_date_btn);
        change_time_btn = findViewById(R.id.change_time_btn);
        date_txt_vw = findViewById(R.id.date_txt_vw);
        time_txt_vw = findViewById(R.id.time_txt_vw);

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                if(year < currentYear || month < currentMonth || day <= currentDay) {
                    Toast.makeText(DateTimeNoteActivity.this,
                            "Reservation Dates must be a day after the current date.",
                            Toast.LENGTH_SHORT).show();
                }

                month += 1;
                String date = month + "/" + day + "/" + year;
                date_txt_vw.setText(date);
            }
        };

        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                boolean isPM = (hourOfDay >= 12);
                time_txt_vw.setText(String.format("%02d:%02d %s",
                        (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12,
                        minute, isPM ? "PM" : "AM"));
            }
        };

        change_date_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(DateTimeNoteActivity.this,
                        mDateSetListener, currentYear, currentMonth, currentDay);
                dialog.show();
            }
        });

        change_time_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);

                TimePickerDialog dialog = new TimePickerDialog(
                        DateTimeNoteActivity.this, mTimeSetListener, hour, min, false);
                dialog.show();
            }
        });
    }
}

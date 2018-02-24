package com.froura.develo4.passenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

public class UpdateAccountActivity extends AppCompatActivity {

    private EditText mobnum_edit_txt;
    private EditText email_edit_txt;
    private Button submit_btn;
    private Button change_trusted_btn;
    private String new_trusted_id;

    private String name;
    private String email;
    private String mobnum;
    private String profpic;
    private String trusted_id;
    private String auth;
    private CountDownTimer typing;

    private boolean mobnum_error_empty = false;
    private boolean mobnum_error_match = false;
    private boolean email_error_empty = false;
    private boolean email_error_match = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_account);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mobnum_edit_txt = findViewById(R.id.mobnum_edit_txt);
        email_edit_txt = findViewById(R.id.email_edit_txt);
        submit_btn = findViewById(R.id.submit_btn);
        change_trusted_btn = findViewById(R.id.change_trusted_btn);
        setDetails();

        new_trusted_id = getIntent().getStringExtra("trusted_id");

        typing = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) { }

            @Override
            public void onFinish() {
                checkErrors();
            }
        };

        mobnum_edit_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mobnum = mobnum_edit_txt.getText().toString().isEmpty() ? "" : mobnum_edit_txt.getText().toString();
                typing.start();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        email_edit_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email = email_edit_txt.getText().toString().isEmpty() ? "" : email_edit_txt.getText().toString();
                typing.start();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkErrors())
                    updateDatabase();
            }
        });

        change_trusted_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(UpdateAccountActivity.this, ChangeTrustedActivity.class);
                //startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkErrors() {
        if(mobnum.isEmpty()) {
            mobnum_error_empty = true;
        } else {
            mobnum_error_empty = false;
        }

        if(mobnum.matches("^(09|\\+639)\\d{9}$")) {
            mobnum_error_match = false;
        } else {
            mobnum_error_match = true;
        }

        if(email.isEmpty()) {
            email_error_empty = true;
        } else {
            email_error_empty = false;
        }

        if(email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            email_error_match = false;
        } else {
            email_error_match = true;
        }
        Log.d("errors", mobnum_error_empty + " " + mobnum_error_match + " " + email_error_empty + " " + email_error_match);
        putErrors();
        return !(mobnum_error_empty && mobnum_error_match && email_error_empty && email_error_empty);
    }

    private void putErrors() {
        if(mobnum_error_empty)
            mobnum_edit_txt.setError("Mobile Number is required.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));
        else if(mobnum_error_match)
            mobnum_edit_txt.setError("Not a valid Philippine Number.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));

        if(email_error_empty)
            email_edit_txt.setError("Email is required.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));
        else if(email_error_match)
            email_edit_txt.setError("Not a valid Email Address.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));

        if(!(mobnum_error_empty && mobnum_error_match)) {
          mobnum_edit_txt.setError(null);
        } else if(!(email_error_empty && email_error_empty)) {
          email_edit_txt.setError(null);
        }
    }

    private void updateDatabase() {
        DatabaseReference pssngrdetails = FirebaseDatabase.getInstance().getReference("users/passenger/" + FirebaseAuth.getInstance().getUid());
        pssngrdetails.child("mobnum").setValue(mobnum);
        pssngrdetails.child("email").setValue(email);
        pssngrdetails.child("profile_pic").setValue(profpic);
        pssngrdetails.child("trusted").setValue(new_trusted_id != null ? trusted_id : new_trusted_id);
        saveUserDetails();
    }

    private void setDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String JSON_DETAILS_KEY = "userDetails";
        String userDetails = sharedPref.getString(JSON_DETAILS_KEY, "{ \"name\" : NULL }");
        try {
            JSONObject jsonObject = new JSONObject(userDetails);
            if(!jsonObject.getString("name").equals("NULL")) {
                name = jsonObject.getString("name");
                email = jsonObject.getString("email");
                mobnum = jsonObject.getString("mobnum");
                profpic = jsonObject.getString("profile_pic");
                trusted_id = jsonObject.getString("trusted_id");
                auth = jsonObject.getString("auth");
                email_edit_txt.setText(email.equals("null") ? "" : email);
                mobnum_edit_txt.setText(mobnum.equals("null") ? "" : mobnum);
            }
        } catch (Exception e) { }
    }

    private void saveUserDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        String JSON_DETAILS_KEY = "userDetails";
        String jsonDetails = "{ \"name\" : \"" + WordUtils.capitalize(name.toLowerCase()) + "\", \"email\" : \"" + email + "\", \"mobnum\" : \"" + mobnum + "\", \"profile_pic\" : \"" + profpic + "\", \"trusted_id\" : " + (new_trusted_id != null ? trusted_id : new_trusted_id) + ", \"auth\" : \"" + auth + "\"}";
        editor.putString(JSON_DETAILS_KEY, jsonDetails);
        editor.apply();
        finish();
    }
}

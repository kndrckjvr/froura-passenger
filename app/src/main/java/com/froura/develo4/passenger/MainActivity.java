package com.froura.develo4.passenger;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.froura.develo4.passenger.libraries.DialogCreator;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements DialogCreator.DialogActionListener,
        SuperTask.TaskListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String name;
    private String email;
    private String mobnum;
    private String profpic;
    private String trusted_id;
    private String auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.loader);

        Glide.with(this).load(getImage("loader")).into(imageView);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }, 10);
                } else {
                    updateUserdetails();
                }
            }
        };
    }

    private void updateUserdetails() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users/passenger/"+FirebaseAuth.getInstance().getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null) {
                    for(DataSnapshot details : dataSnapshot.getChildren()) {
                        if(details.getKey().equals("auth")) auth = details.getValue().toString();
                        if(details.getKey().equals("email")) email = details.getValue().toString();
                        if(details.getKey().equals("mobnum")) mobnum = details.getValue().toString();
                        if(details.getKey().equals("name")) name = details.getValue().toString();
                        if(details.getKey().equals("profile_pic")) profpic = details.getValue().toString();
                        if(details.getKey().equals("trusted")) trusted_id = details.getValue().toString();
                    }
                    saveUserDetails();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    private void saveUserDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        String JSON_DETAILS_KEY = "userDetails";
        Log.d("setac", profpic);
        String jsonDetails = "{ \"name\" : \"" + WordUtils.capitalize(name.toLowerCase()) + "\", \"email\" : \"" + email + "\", \"mobnum\" : \"" + mobnum + "\", \"profile_pic\" : \"" + profpic + "\", \"trusted_id\" : " + trusted_id + ", \"auth\" : \"" + auth + "\"}";
        editor.putString(JSON_DETAILS_KEY, jsonDetails);
        editor.apply();
        Intent intent = new Intent(MainActivity.this, LandingActivity.class);
        startActivity(intent);
        finish();
        //SuperTask.execute(MainActivity.this, TaskConfig.CHECK_CONNECTION_URL, "check_connection");
    }

    @Override
    public void onTaskRespond(String json, String id, int resultcode) {
        if(resultcode == 503) {
            /*Intent intent = new Intent(MainActivity.this, SMSActivity.class);
            startactivity(intent);
            finish();*/
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.getString("status").equals("SUCCESS")) {
                Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) { }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        contentValues.put("android", 1);
        return contentValues;
    }

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources()
                .getIdentifier(imageName, "drawable", this.getPackageName());

        return drawableResourceId;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onClickPositiveButton(String actionId) { }

    @Override
    public void onClickNegativeButton(String actionId) { }

    @Override
    public void onClickNeutralButton(String actionId) { }

    @Override
    public void onClickMultiChoiceItem(String actionId, int which, boolean isChecked) { }

    @Override
    public void onCreateDialogView(String actionId, View view) { }
}

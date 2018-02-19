package com.froura.develo4.passenger;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.froura.develo4.passenger.config.TaskConfig;
import com.froura.develo4.passenger.libraries.DialogCreator;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements DialogCreator.DialogActionListener,
        SuperTask.TaskListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

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
                            Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }, 10);
                } else {
                    SuperTask.execute(MainActivity.this, TaskConfig.CHECK_CONNECTION_URL, "", false);
                }
            }
        };
    }

    @Override
    public void onTaskRespond(String json, int resultcode) {
        if(resultcode == 503) {
            /*Intent intent = new Intent(MainActivity.this, SMSActivity.class);
            startactivity(intent);
            finish();*/
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.getString("status").equals("SUCCESS")) {
                Intent intent = new Intent(MainActivity.this, BookingActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) { }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues) {
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

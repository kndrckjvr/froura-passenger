package com.froura.develo4.passenger;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.froura.develo4.passenger.config.TaskConfig;
import com.froura.develo4.passenger.tasks.SuperTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneAuthentication extends AppCompatActivity implements SuperTask.TaskListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView requestCode;
    private TextView mob_num;
    private Button verify;
    private EditText verifCode;
    private ProgressDialog progressDialog;

    private String mobnum;
    private String email;
    private String name;
    private String profpic = "default";
    private String trusted_id = "null";
    private String database_id = "null";
    private String auth = "mobile";
    private boolean phoneReg;
    private CountDownTimer requestCodeTimer;

    private String TAG = "PhoneAuth";
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_authentication);

        requestCode = findViewById(R.id.txtVw_request_code);
        verify = findViewById(R.id.btn_verify);
        verifCode = findViewById(R.id.et_verif_code);
        mob_num = findViewById(R.id.txtVw_mob_num);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Logging in with Mobile...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);

        mobnum = getIntent().getStringExtra("mobnum");
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        phoneReg = getIntent().getBooleanExtra("phoneReg", false);
        
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser() != null) {
                    registerUser();
                    return;
                }
            }
        };

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mCode = verifCode.getText().toString();
                if(!mCode.isEmpty()) {
                    signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(mVerificationId, mCode));
                    verifCode.setError(null);
                    verifCode.setCompoundDrawablesWithIntrinsicBounds(0,0, 0,0);
                } else {
                    verifCode.setError("Code is required.", getResources().getDrawable(R.drawable.ic_warning_red_24dp));
                }

            }
        });

        requestCodeTimer = new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long l) {
                        requestCode.setText("Request a new code in 00:"+ l/1000);
                        requestCode.setTextColor(getResources().getColor(R.color.textViewColor));
                    }

                    @Override
                    public void onFinish() {
                        requestCode.setText(Html.fromHtml("<u>Request a new code.</u>"));
                        requestCode.setTextColor(getResources().getColor(R.color.textLinkColor));

                        requestCode.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestCode();
                            }
                        });
                    }
                };

        if(mobnum.matches("^(09)\\d{9}$")) {
            mob_num.setText("+63 " + mobnum.substring(1));
        } else if(mobnum.matches("^(\\+639)\\d{9}$")) {
            mob_num.setText("+63 " + mobnum.substring(3));
        }

        if(phoneReg) {
            requestCode();
            requestCode.setOnClickListener(null);
            requestCodeTimer.start();
            phoneReg = false;
        }
    }

    private void saveUserDetails() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        String JSON_DETAILS_KEY = "userDetails";
        String jsonDetails = "{ \"name\" : \"" + WordUtils.capitalize(name.toLowerCase()) + "\", \"email\" : \"" + email + "\", \"mobnum\" : \"" + mobnum + "\", \"profile_pic\" : \"" + profpic + "\", \"trusted_id\" : " + WordUtils.capitalize(trusted_id) + ", \"auth\" : \"" + auth + "\", \"database_id\": \" "+ database_id +"\"}";
        editor.putString(JSON_DETAILS_KEY, jsonDetails);
        editor.apply();
        progressDialog.dismiss();
        Intent intent = new Intent(PhoneAuthentication.this, LandingActivity.class);
        startActivity(intent);
        finish();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            registerUser();
                        }
                    }
                });
    }
    
    private void requestCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobnum,
                60,
                TimeUnit.SECONDS,
                PhoneAuthentication.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        progressDialog.show();
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.w(TAG, "onVerificationFailed", e);
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            //Phone number is incorrect
                            Toast.makeText(PhoneAuthentication.this, "Phone number is incorrect!", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            //Quota has been reached
                            Toast.makeText(PhoneAuthentication.this, "Server Overload! A request has been sent.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        mVerificationId = s;
                    }
                });
    }

    private void registerUser() {
        String user_id = mAuth.getCurrentUser().getUid();
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child("passenger").child(user_id);
        dbRef.child("name").setValue(WordUtils.capitalize(name.toLowerCase()));
        dbRef.child("auth").setValue(auth);
        dbRef.child("profile_pic").setValue(profpic);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                    if(value.get("mobnum") != null) {
                        if(!value.get("mobnum").toString().equals("null")) {
                            mobnum = value.get("mobnum").toString();
                        } else {
                            mobnum = "null";
                        }
                    } else {
                        dbRef.child("mobnum").setValue(mobnum);
                    }

                    if(value.get("email") != null) {
                        if(!value.get("email").toString().equals("null")) {
                            email = value.get("email").toString();
                        } else {
                            email= "null";
                        }
                    } else {
                        dbRef.child("email").setValue(email);
                    }
                    if(value.get("trusted") != null) {
                        if(!value.get("trusted").toString().equals("null")) {
                            trusted_id = value.get("trusted").toString();
                        } else {
                            trusted_id = "null";
                        }
                    } else {
                        dbRef.child("trusted").setValue(trusted_id);
                    }
                    SuperTask.execute(PhoneAuthentication.this,
                            TaskConfig.REGISTER_USER_URL,
                            "register");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
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
    public void onTaskRespond(String json, String id) {
        switch (id) {
            case "register":
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String status = jsonObject.getString("status");
                    if(status.equals("success")) {
                        database_id = jsonObject.getString("database_id");
                    } else {
                        Toast.makeText(this, jsonObject.getString("status"), Toast.LENGTH_SHORT).show();
                    }
                    saveUserDetails();
                } catch (Exception e) { }
                break;
        }
    }

    @Override
    public ContentValues setRequestValues(ContentValues contentValues, String id) {
        switch (id) {
            case "register":
                contentValues.put("android", 1);
                contentValues.put("firebase_uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                contentValues.put("name", name);
                contentValues.put("email", email);
                contentValues.put("contact", mobnum);
                contentValues.put("img_path", profpic);
                return contentValues;
            default:
                return null;
        }
    }
}

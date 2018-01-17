package com.froura.develo4.passenger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

public class LandingActivity extends AppCompatActivity {

    private Button mobLogin;
    private Button googLogin;
    private Button faceLogin;
    private GoogleSignInClient mGoogleSignInClient;
    private LoginButton loginButton;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String email = "null";
    private String name = "null";
    private String profpic = "null";
    private String mobnum = "null";
    private String auth;
    private static final int RC_SIGN_IN = 1;

    private CallbackManager mCallbackManager;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        mobLogin = findViewById(R.id.mobLogin);
        googLogin = findViewById(R.id.googLogin);
        faceLogin = findViewById(R.id.faceLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);

        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);

        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookSignin(loginResult.getAccessToken());
                progressDialog.setMessage("Logging in with Facebook...");
                progressDialog.show();
            }

            @Override
            public void onCancel() {
                Log.e("fbLogin", "facebookLoginCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("fbLogin", "facebookLoginError: " + error.getMessage());
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mobLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LandingActivity.this, PhoneRegistration.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        googLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignin();
            }
        });

        faceLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.performClick();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser() != null) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(LandingActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
    }

    private void googleSignin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void facebookSignin(AccessToken token) {
        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            if(object.has("email")) email = object.getString("email");
                            if(object.has("id")) profpic = "https://graph.facebook.com/"
                                    + object.getString("id") + "/picture?width=500&height=500";
                            if(object.has("name")) name = object.getString("name");
                            auth = "facebook";
                        } catch (Exception ignored) { }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        userExist(mAuth.getCurrentUser().getUid());
                    }
                });
    }

    private void userExist(String user_id) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child("passenger").child(user_id);
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    registerUser();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("firebaseDB", "userExistError: "+databaseError.getMessage());
            }
        });
    }

    private void registerUser() {
        String user_id = mAuth.getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child("passenger").child(user_id);
        dbRef.child("name").setValue(name);
        dbRef.child("email").setValue(email);
        dbRef.child("mobnum").setValue(mobnum);
        dbRef.child("auth").setValue(auth);
        dbRef.child("profile_pic").setValue(profpic);
    }

    private void saveUserDetails() {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String jsonDetails = "{ \"name\" : " + name + ", \"email\" : " + email + ", \"mobnum\" : "+ mobnum +", \"profile_pic\" : " + profpic + "}";
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        userExist(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                name = account.getDisplayName();
                email = account.getEmail();
                profpic = account.getPhotoUrl().toString();
                auth = "google";
                progressDialog.setMessage("Logging in with Google...");
                progressDialog.show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) { Log.d("ApiError", e.getMessage()); }
        }
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private String tag = "LoginActivity";
    EditText loginEmail, loginPassword;
    Button loginButton, signupButton, newPassButton;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.email);
        loginPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.logInButton);
        signupButton = findViewById(R.id.signUpButton);
        SignInButton signInWithGoogleButton = findViewById(R.id.sign_in__google_button);
        newPassButton = findViewById(R.id.forgottenPasswordButton);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(LoginActivity.this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInWithGoogleButton.setOnClickListener(v -> GooglesignIn());

        loginButton.setOnClickListener(v -> {
            if (loginEmail.getText().toString().isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter an email.",
                        Toast.LENGTH_SHORT).show();
            } else if (loginPassword.getText().toString().isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter a password.",
                        Toast.LENGTH_SHORT).show();
            } else {
                signIn(loginEmail.getText().toString(), loginPassword.getText().toString());
            }
        });
        signupButton.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        newPassButton.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), NewPasswordActivity.class)));

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), ProfileScreen.class));
            finish();
        }

    }

    private void GooglesignIn() {
        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signIntent, RC_SIGN_IN);
    }

    @SuppressLint("LogNotTimber")
    private void signIn(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(tag, "signInWithEmail:success");
                        startActivity(new Intent(getApplicationContext(), ProfileScreen.class));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(tag, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                assert account != null;
                authWithGoogle(account);
            }
        }
    }

    private void authWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                assert user != null;
                DocumentReference docRef = db.collection("users").document(user.getUid());

                docRef.get().addOnCompleteListener(task1 -> {
                    if (!Objects.requireNonNull(task1.getResult()).contains("email")) {
                        // If this is the first time the user has signed with this Google account, set the default values for the fields in the database.
                        docRef.set(new User(account.getEmail(), "", 0.0));
                        docRef.update("provider", "Google");
                        docRef.update("coinsLeft", "25");
                        docRef.update("wallet", new ArrayList<String>());
                        docRef.update("coinsLeft", 25);
                        docRef.update("magnetUnlocked", false);
                        docRef.update("stealUnlocked", false);
                        docRef.update("shieldUnlocked", false);
                        docRef.update("magnetMode", false);
                        docRef.update("stealUsed", false);
                        docRef.update("piggybankProtected", false);
                        docRef.update("cantStealFrom", new ArrayList<String>());
                        docRef.update("piggybank", new ArrayList<String>());
                        docRef.update("treasureUnlocked", false);
                        docRef.update("treasureFound", false);
                        startActivity(new Intent(getApplicationContext(), UsernameActivity.class));
                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), ProfileScreen.class));
                        finish();
                    }
                });
            }
            else {
                Toast.makeText(getApplicationContext(), "Auth Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed.", Toast.LENGTH_SHORT).show();
    }
}
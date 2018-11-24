package com.example.nikoleta.coinz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends Activity {
    EditText email,password;
    Button signUpButton,loginButton;
    FirebaseAuth mAuth;
    private String mEmail, mPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        loginButton = (Button) findViewById(R.id.memberButton);


        mAuth = FirebaseAuth.getInstance();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout(width, (int)(height * .65));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmail = email.getText().toString();
                mPassword = password.getText().toString();
                if(mEmail.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please enter an email.",
                            Toast.LENGTH_SHORT).show();
                }
                else if(mPassword.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please enter a password.",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    createAccount(mEmail, mPassword);
                }
                }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),ProfileScreen.class));
        }
    }

    private void createAccount(String email, String password) {
        Log.d("SignUpActivity", "createAccount:" + email);
        if(TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
        }

        if(password.length()<6){
            Toast.makeText(getApplicationContext(),"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
        }

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            Log.d("SignUpActivity", "createUserWithEmail:success");
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            db.collection("users").document(user.getUid()).set(new User(email, password));

                            startActivity(new Intent(getApplicationContext(),UsernameActivity.class));
                            finish();
                        }
                        else{
                            Log.w("SignUpActivity", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Couldn't register. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
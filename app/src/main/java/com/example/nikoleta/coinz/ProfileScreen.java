package com.example.nikoleta.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ProfileScreen extends AppCompatActivity {
    TextView textView;
    Button btnDeleteUser,btnLogout, btnPlay, btnGameRules, btnExit;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener  authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);
        textView = (TextView) findViewById(R.id.textView1);
        btnDeleteUser = (Button) findViewById(R.id.delete);
        btnLogout = (Button) findViewById(R.id.logout);
        btnPlay = (Button) findViewById(R.id.play);
        btnGameRules = (Button) findViewById(R.id.rules);
        btnExit = (Button) findViewById(R.id.exit);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user  = firebaseAuth.getCurrentUser()  ;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String username = user.getDisplayName();

        // If the user is not signed in with Google
        if (username == null || username.equals("")) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("ProfileScreen", "DocumentSnapshot data: " + document.getData());
                            String username = document.getString("username");
                            textView.setText("Hello " + username + "!");
                        } else {
                            Log.d("ProfileScreen", "No such document");
                        }
                    } else {
                        Log.d("ProfileScreen", "get failed with ", task.getException());
                    }
                }
            });
        }

        // If the user is signed in with Google.
        else {
            textView.setText("Hello " + username + "!");
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null){
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }
        };



        btnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user!=null){
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(),"User deleted",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MapActivity.class));
                finish();
            }
        });

        btnGameRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), GameRulesActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
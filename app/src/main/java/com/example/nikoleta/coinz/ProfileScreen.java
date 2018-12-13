package com.example.nikoleta.coinz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileScreen extends AppCompatActivity {
    TextView textView;
    Button btnDeleteUser,btnLogout, btnPlay, btnGameRules, btnExit;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener  authStateListener;

    @SuppressLint({"LogNotTimber", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);
        textView = findViewById(R.id.textView1);
        btnDeleteUser = findViewById(R.id.delete);
        btnLogout = findViewById(R.id.logout);
        btnPlay = findViewById(R.id.play);
        btnGameRules = findViewById(R.id.rules);
        btnExit = findViewById(R.id.exit);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user  = firebaseAuth.getCurrentUser()  ;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        assert user != null;
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
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
        });

        authStateListener = firebaseAuth -> {
            FirebaseUser user1 = firebaseAuth.getCurrentUser();
            if(user1 == null){
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        };

        btnDeleteUser.setOnClickListener(v -> user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "User deleted", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                finish();
            }
        }));

        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        });

        btnPlay.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),MapActivity.class));
            finish();
        });

        btnGameRules.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), GameRulesActivity.class)));
        btnExit.setOnClickListener(v -> finish());
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
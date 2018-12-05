package com.example.nikoleta.coinz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class BoostersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boosters);
        Button btnMagnet = (Button) findViewById(R.id.magnet_booster);
        Button btnSteal = (Button) findViewById(R.id.steal_booster);
        Button btnShield = (Button) findViewById(R.id.protection_booster);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (Objects.requireNonNull(task.getResult()).contains("magnetUnlocked")) {
                    if (task.getResult().getBoolean("magnetUnlocked")) {
                        btnMagnet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.magnet, 0, R.drawable.unlocked, 0);
                    }
                }
                if (task.getResult().contains("stealUnlocked")) {
                    if (task.getResult().getBoolean("stealUnlocked")) {
                        btnSteal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thief, 0, R.drawable.unlocked, 0);
                    }
                }
                if (task.getResult().contains("shieldUnlocked")) {
                    if (task.getResult().getBoolean("shieldUnlocked")) {
                        btnShield.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shield, 0, R.drawable.unlocked, 0);
                    }
                }
            }
        });

        btnMagnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable1 = btnMagnet.getCompoundDrawables()[2];
                Drawable drawable2 = getDrawable(R.drawable.locked);
                Bitmap bitmap1 = ((BitmapDrawable)drawable1).getBitmap();
                Bitmap bitmap2 = ((BitmapDrawable)drawable2).getBitmap();
                if (bitmap1 == bitmap2){
                    Toast.makeText(getApplicationContext(), "This Booster is locked. To unlock it you need to have a certain collection of coins in your Piggybank. Go to your Piggybank to learn more.", Toast.LENGTH_LONG).show();
                }
                else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    DocumentReference docRef = db.collection("users").document(user.getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (Objects.requireNonNull(task.getResult()).contains("magnetMode")) {
                                if (task.getResult().getBoolean("magnetMode")) {
                                    Toast.makeText(getApplicationContext(), "You are already using this Booster.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    startActivity(new Intent(getApplicationContext(), MagnetActivity.class));
                                }
                            }
                            else {
                                startActivity(new Intent(getApplicationContext(), MagnetActivity.class));
                            }
                        }
                    });

                }
            }
        });


        btnSteal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable1 = btnSteal.getCompoundDrawables()[2];
                Drawable drawable2 = getDrawable(R.drawable.locked);
                Bitmap bitmap1 = ((BitmapDrawable)drawable1).getBitmap();
                Bitmap bitmap2 = ((BitmapDrawable)drawable2).getBitmap();
                if (bitmap1 == bitmap2){
                    Toast.makeText(getApplicationContext(), "This Booster is locked. To unlock it you need to have a certain collection of coins in your Piggybank. Go to your Piggybank to learn more.", Toast.LENGTH_LONG).show();
                }
                else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    DocumentReference docRef = db.collection("users").document(user.getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (Objects.requireNonNull(task.getResult()).contains("stealUsed")) {
                                if (task.getResult().getBoolean("stealUsed")) {
                                    Toast.makeText(getApplicationContext(), "You have already used this Booster.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    startActivity(new Intent(getApplicationContext(), StealActivity.class));
                                }
                            }
                            else {
                                startActivity(new Intent(getApplicationContext(), StealActivity.class));
                            }
                        }
                    });
                }
            }
        });


        btnShield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable1 = btnShield.getCompoundDrawables()[2];
                Drawable drawable2 = getDrawable(R.drawable.locked);
                Bitmap bitmap1 = ((BitmapDrawable)drawable1).getBitmap();
                Bitmap bitmap2 = ((BitmapDrawable)drawable2).getBitmap();
                if (bitmap1 == bitmap2){
                    Toast.makeText(getApplicationContext(), "This Booster is locked. To unlock it you need to have a certain collection of coins in your Piggybank. Go to your Piggybank to learn more.", Toast.LENGTH_LONG).show();
                }
                else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    DocumentReference docRef = db.collection("users").document(user.getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (Objects.requireNonNull(task.getResult()).contains("piggybankProtected")) {
                                if (task.getResult().getBoolean("piggybankProtected")) {
                                    Toast.makeText(getApplicationContext(), "You have already used this Booster.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    startActivity(new Intent(getApplicationContext(), ShieldActivity.class));
                                }
                            }
                            else {
                                startActivity(new Intent(getApplicationContext(), ShieldActivity.class));
                            }
                        }
                    });
                }
            }
        });

    }
}

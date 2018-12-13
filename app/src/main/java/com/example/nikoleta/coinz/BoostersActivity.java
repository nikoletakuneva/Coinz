package com.example.nikoleta.coinz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class BoostersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boosters);
        Button btnMagnet = findViewById(R.id.magnet_booster);
        Button btnSteal = findViewById(R.id.steal_booster);
        Button btnShield = findViewById(R.id.protection_booster);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        assert user != null;
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (Objects.requireNonNull(task.getResult()).contains("magnetUnlocked")) {
                if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task).getResult()).get("magnetUnlocked")) {
                    // Set unlocked icon for the Booster.
                    btnMagnet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.magnet, 0, R.drawable.unlocked, 0);
                }
            }
            if (task.getResult().contains("stealUnlocked")) {
                if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task).getResult()).get("stealUnlocked")) {
                    // Set unlocked icon for the Booster.
                    btnSteal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.thief, 0, R.drawable.unlocked, 0);
                }
            }
            if (task.getResult().contains("shieldUnlocked")) {
                if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task).getResult()).get("shieldUnlocked")) {
                    // Set unlocked icon for the Booster.
                    btnShield.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shield, 0, R.drawable.unlocked, 0);
                }
            }
        });

        btnMagnet.setOnClickListener(view -> {
            Drawable drawable1 = btnMagnet.getCompoundDrawables()[2];
            Drawable drawable2 = getDrawable(R.drawable.locked);
            Bitmap bitmap1 = ((BitmapDrawable)drawable1).getBitmap();
            assert drawable2 != null;
            Bitmap bitmap2 = ((BitmapDrawable)drawable2).getBitmap();

            // Check if Booster is unlocked by comparing the icons
            if (bitmap1 == bitmap2){
                Toast.makeText(getApplicationContext(), "This Booster is locked. To unlock it you need to have a certain collection of coins in your Piggybank. Go to your Piggybank to learn more.", Toast.LENGTH_LONG).show();
            }
            else {
                FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth1 = FirebaseAuth.getInstance();
                FirebaseUser user1 = firebaseAuth1.getCurrentUser();

                assert user1 != null;
                DocumentReference docRef1 = db1.collection("users").document(user1.getUid());
                docRef1.get().addOnCompleteListener(task -> {
                    if (Objects.requireNonNull(task.getResult()).contains("magnetMode")) {
                        if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task).getResult()).get("magnetMode")) {
                            Toast.makeText(getApplicationContext(), "You are already using this Booster.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            startActivity(new Intent(getApplicationContext(), MagnetActivity.class));
                        }
                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), MagnetActivity.class));
                    }
                });

            }
        });


        btnSteal.setOnClickListener(view -> {
            Drawable drawable1 = btnSteal.getCompoundDrawables()[2];
            Drawable drawable2 = getDrawable(R.drawable.locked);
            Bitmap bitmap1 = ((BitmapDrawable)drawable1).getBitmap();
            assert drawable2 != null;
            Bitmap bitmap2 = ((BitmapDrawable)drawable2).getBitmap();

            // Check if Booster is unlocked by comparing the icons
            if (bitmap1 == bitmap2){
                Toast.makeText(getApplicationContext(), "This Booster is locked. To unlock it you need to have a certain collection of coins in your Piggybank. Go to your Piggybank to learn more.", Toast.LENGTH_LONG).show();
            }
            else {
                FirebaseFirestore db12 = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth12 = FirebaseAuth.getInstance();
                FirebaseUser user12 = firebaseAuth12.getCurrentUser();

                assert user12 != null;
                DocumentReference docRef12 = db12.collection("users").document(user12.getUid());
                docRef12.get().addOnCompleteListener(task -> {
                    if (Objects.requireNonNull(task.getResult()).contains("stealUsed")) {
                        if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task).getResult()).get("stealUsed")) {
                            Toast.makeText(getApplicationContext(), "You have already used this Booster.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            startActivity(new Intent(getApplicationContext(), StealActivity.class));
                        }
                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), StealActivity.class));
                    }
                });
            }
        });


        btnShield.setOnClickListener(view -> {
            Drawable drawable1 = btnShield.getCompoundDrawables()[2];
            Drawable drawable2 = getDrawable(R.drawable.locked);
            Bitmap bitmap1 = ((BitmapDrawable)drawable1).getBitmap();
            assert drawable2 != null;
            Bitmap bitmap2 = ((BitmapDrawable)drawable2).getBitmap();

            // Check if Booster is unlocked by comparing the icons
            if (bitmap1 == bitmap2){
                Toast.makeText(getApplicationContext(), "This Booster is locked. To unlock it you need to have a certain collection of coins in your Piggybank. Go to your Piggybank to learn more.", Toast.LENGTH_LONG).show();
            }
            else {
                FirebaseFirestore db13 = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth13 = FirebaseAuth.getInstance();
                FirebaseUser user13 = firebaseAuth13.getCurrentUser();

                assert user13 != null;
                DocumentReference docRef13 = db13.collection("users").document(user13.getUid());
                docRef13.get().addOnCompleteListener(task -> {
                    if (Objects.requireNonNull(task.getResult()).contains("piggybankProtected")) {
                        if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task).getResult()).get("piggybankProtected")) {
                            Toast.makeText(getApplicationContext(), "You have already used this Booster.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            startActivity(new Intent(getApplicationContext(), ShieldActivity.class));
                        }
                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), ShieldActivity.class));
                    }
                });
            }
        });

    }
}

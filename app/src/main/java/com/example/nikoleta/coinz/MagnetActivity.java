package com.example.nikoleta.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MagnetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnet);

        Button use_magnet = findViewById(R.id.use_magnet);
        use_magnet.setOnClickListener(view -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            assert user != null;
            db.collection("users").document(user.getUid()).update("magnetMode", true);
            Toast.makeText(getApplicationContext(), "You can now use the Magnet Booster.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

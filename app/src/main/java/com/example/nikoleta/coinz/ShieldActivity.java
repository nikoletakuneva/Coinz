package com.example.nikoleta.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShieldActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shield);

        Button use_steal = findViewById(R.id.use_shield);
        use_steal.setOnClickListener(view -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            assert user != null;
            db.collection("users").document(user.getUid()).update("piggybankProtected", true);
            Toast.makeText(getApplicationContext(), "You are now protected from theft until the end of the day.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

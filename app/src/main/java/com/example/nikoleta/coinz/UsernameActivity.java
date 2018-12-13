package com.example.nikoleta.coinz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;
import java.util.Objects;

public class UsernameActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        Button btnUsername = findViewById(R.id.usernameButton);
        EditText usernameText = findViewById(R.id.username);


        btnUsername.setOnClickListener(view -> {
            String username = usernameText.getText().toString();

            // Check whether the username already exists
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query equal = db.collection("users").whereEqualTo("username", username);
            Task<QuerySnapshot> snapshotTask = equal.get();
            snapshotTask.addOnCompleteListener(task -> {
                List<DocumentSnapshot> documentsList = Objects.requireNonNull(snapshotTask.getResult()).getDocuments();
                // Username doesn't exist in the database
                if (documentsList.isEmpty()) {
                    if (username.length() <= 20 && !username.isEmpty()) {
                        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        assert user != null;
                        db1.collection("users").document(user.getUid()).update("username", username);
                        startActivity(new Intent(getApplicationContext(), ProfileScreen.class));
                        finish();
                    }
                    else {
                        Toast.makeText(UsernameActivity.this, "Username must contain at most 20 characters.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                //Username is already taken by another user
                else {
                    Toast.makeText(UsernameActivity.this, "Username is already taken by another user.",
                            Toast.LENGTH_SHORT).show();
                }

            });
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(UsernameActivity.this, "Please enter a username.",
                Toast.LENGTH_SHORT).show();
    }
}

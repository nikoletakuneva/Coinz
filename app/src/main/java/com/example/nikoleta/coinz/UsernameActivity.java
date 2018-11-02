package com.example.nikoleta.coinz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UsernameActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        Button btnUsername = (Button) findViewById(R.id.usernameButton);
        EditText usernameText = (EditText) findViewById(R.id.username);


        btnUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameText.getText().toString();
                if (username.length() <= 20 && !username.isEmpty()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    db.collection("users").document(user.getUid()).update("username", username);
                    startActivity(new Intent(getApplicationContext(), ProfileScreen.class));
                    finish();
                }
                else {
                    Toast.makeText(UsernameActivity.this, "Username must contain 20 characters at most",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(UsernameActivity.this, "Please enter a username.",
                Toast.LENGTH_SHORT).show();
    }
}

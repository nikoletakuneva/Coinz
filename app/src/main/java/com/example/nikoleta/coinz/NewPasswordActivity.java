package com.example.nikoleta.coinz;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class NewPasswordActivity extends Activity {
    EditText mEmail;
    Button btnNewPass;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
        mEmail = findViewById(R.id.email);
        btnNewPass = findViewById(R.id.newPasswordButton);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout(width, (int)(height * .40));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);

        firebaseAuth = FirebaseAuth.getInstance();

        btnNewPass.setOnClickListener(v -> {
            String email = mEmail.getText().toString();

            if(TextUtils.isEmpty(email)){
                Toast.makeText(getApplicationContext(),"Please enter an email.",Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(NewPasswordActivity.this, "Password reset link was sent to your email address",
                            Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(NewPasswordActivity.this::finish, 2000);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Mail sending error", Toast.LENGTH_SHORT).show();
                }
            });
        });


    }
}
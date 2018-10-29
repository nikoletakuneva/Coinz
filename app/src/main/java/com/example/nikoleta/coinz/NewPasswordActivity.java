package com.example.nikoleta.coinz;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class NewPasswordActivity extends AppCompatActivity {
    EditText mEmail;
    Button btnNewPass;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
        mEmail = (EditText) findViewById(R.id.email);
        btnNewPass = (Button) findViewById(R.id.newPasswordButton);

        firebaseAuth = FirebaseAuth.getInstance();

        btnNewPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(),"Please fill e-mail",Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
//                                    Toast.makeText(getApplicationContext(),"Password reset link was sent your email address",Toast.LENGTH_LONG).show();
//                                    //finish();
                                    Toast.makeText(NewPasswordActivity.this, "Password reset link was sent to your email address",
                                            Toast.LENGTH_SHORT).show();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            NewPasswordActivity.this.finish();
                                        }
                                    }, 2000);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"Mail sending error",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


    }
}
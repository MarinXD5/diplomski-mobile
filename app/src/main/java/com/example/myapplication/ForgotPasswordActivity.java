package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends BaseActivity{
    EditText email;

    Button resetPWD;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildLayout(R.layout.activity_forgot_password);

        email = findViewById(R.id.resetPWDEmailInput);
        resetPWD = findViewById(R.id.resetPasswordButton);

        resetPWD.setOnClickListener(view -> {
            String email;

            email = String.valueOf(this.email.getText());

            if(TextUtils.isEmpty(email)){
                Toast.makeText(ForgotPasswordActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "If the email exists we will send you a email to reset your password!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                }
                else{
                    Toast.makeText(ForgotPasswordActivity.this, "Email does not exist. Please check your input and try again!", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }
}

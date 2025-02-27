package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends BaseActivity{
    EditText email, password;
    Button login;
    FirebaseAuth mAuth;

    TextView signUp, forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildLayout(R.layout.login_activity);

        email = findViewById(R.id.emailInputField);
        password = findViewById(R.id.passwordInputField);
        login = findViewById(R.id.loginButton);
        signUp = findViewById(R.id.signUpText);
        forgotPassword = findViewById(R.id.forgotPasswordText);
        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(view -> {
            String email, password;

            email = String.valueOf(this.email.getText());
            password = String.valueOf(this.password.getText());

            if(TextUtils.isEmpty(email)){
                Toast.makeText(LoginActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if(TextUtils.isEmpty(password)){
                Toast.makeText(LoginActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "You have successfully logged in!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
                else{
                    Toast.makeText(LoginActivity.this, "There was an error while logging in! Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        signUp.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        forgotPassword.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }
}

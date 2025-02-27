package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity{

    EditText email, password;
    Button signUp;
    TextView loginText;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildLayout(R.layout.activity_register);

        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        signUp = findViewById(R.id.signUpButton);
        loginText = findViewById(R.id.loginText);
        mAuth = FirebaseAuth.getInstance();

        loginText.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        signUp.setOnClickListener(view -> {
            String email, password;
            email = String.valueOf(this.email.getText());
            password = String.valueOf(this.password.getText());

            System.out.println("Email: " + email);
            System.out.println("Password: " + password);

            if(TextUtils.isEmpty(email)){
                Toast.makeText(RegisterActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if(TextUtils.isEmpty(password)){
                Toast.makeText(RegisterActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();

                    if(user != null){
                        user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                            if(verifyTask.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "We've sent you an email to confirm entered email!", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            }
                        });
                    }
                    Toast.makeText(RegisterActivity.this, "You have successfully registered!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(RegisterActivity.this, "There was an error while registering! Please try again.", Toast.LENGTH_SHORT).show();
                }
            });

        });

    }
}

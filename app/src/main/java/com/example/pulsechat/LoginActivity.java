package com.example.pulsechat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pulsechat.databinding.ActivityLoginBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        b.tvForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        b.btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = b.etEmail.getText().toString().trim();
        String pass = b.etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            b.etEmail.setError("Enter a valid email");
            return;
        }
        if (pass.isEmpty()) {
            b.etPassword.setError("Enter your password");
            return;
        }
        if (!Utils.isOnline(this)) {
            Snackbar.make(b.getRoot(), "No internet connection", Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(b.getRoot(), "Login failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        b.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        b.btnLogin.setEnabled(!loading);
    }
}

package com.example.pulsechat;

import android.os.Bundle;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pulsechat.databinding.ActivityForgotBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityForgotBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.tvBack.setOnClickListener(v -> finish());
        b.btnSend.setOnClickListener(v -> {
            String email = b.etEmail.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                b.etEmail.setError("Enter a valid email");
                return;
            }
            b.btnSend.setEnabled(false);
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(r -> {
                        b.btnSend.setEnabled(true);
                        Snackbar.make(b.getRoot(), "If that account exists, a reset email was sent.",
                                Snackbar.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        b.btnSend.setEnabled(true);
                        Snackbar.make(b.getRoot(), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
        });
    }
}

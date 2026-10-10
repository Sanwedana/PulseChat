package com.example.pulsechat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View logo = findViewById(R.id.logo);
        View name = findViewById(R.id.appName);
        View tag = findViewById(R.id.tagline);
        logo.setScaleX(0.4f); logo.setScaleY(0.4f); logo.setAlpha(0f);
        name.setAlpha(0f); tag.setAlpha(0f);

        logo.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(700).start();
        name.animate().alpha(1f).setStartDelay(400).setDuration(500).start();
        tag.animate().alpha(1f).setStartDelay(700).setDuration(500).start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
            startActivity(new Intent(this, loggedIn ? MainActivity.class : LoginActivity.class));
            finish();
        }, 1800);
    }
}

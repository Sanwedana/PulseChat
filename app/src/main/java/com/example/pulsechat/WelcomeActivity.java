package com.example.pulsechat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pulsechat.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWelcomeBinding b = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.btnStart.setOnClickListener(v -> {
            getSharedPreferences("pulse", Context.MODE_PRIVATE)
                    .edit().putBoolean("seen_welcome", true).apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}

package com.example.pulsechat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pulsechat.databinding.ActivityRegisterBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding b;
    private int avatarId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        View.OnClickListener pick = v -> AvatarPicker.show(this, id -> {
            avatarId = id;
            b.ivAvatar.setImageResource(Utils.avatarRes(id));
        });
        b.ivAvatar.setOnClickListener(pick);
        b.tvPickAvatar.setOnClickListener(pick);

        b.tvLogin.setOnClickListener(v -> finish());
        b.btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String name = b.etName.getText().toString().trim();
        String email = b.etEmail.getText().toString().trim();
        String pass = b.etPassword.getText().toString();

        if (name.isEmpty()) { b.etName.setError("Enter your name"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { b.etEmail.setError("Enter a valid email"); return; }
        if (pass.length() < 6) { b.etPassword.setError("At least 6 characters"); return; }
        if (!Utils.isOnline(this)) {
            Snackbar.make(b.getRoot(), "No internet connection", Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) { setLoading(false); return; }

                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build())
                            .addOnCompleteListener(t -> {
                                Map<String, Object> profile = new HashMap<>();
                                profile.put("uid", user.getUid());
                                profile.put("name", name);
                                profile.put("nameLower", name.toLowerCase());
                                profile.put("email", email);
                                profile.put("status", "Hey there! I'm using PulseChat 🌸");
                                profile.put("avatarId", avatarId);
                                profile.put("createdAt", FieldValue.serverTimestamp());

                                FirebaseFirestore.getInstance().collection("users")
                                        .document(user.getUid()).set(profile)
                                        .addOnSuccessListener(v -> {
                                            startActivity(new Intent(this, MainActivity.class));
                                            finishAffinity();
                                        })
                                        .addOnFailureListener(e -> {
                                            setLoading(false);
                                            Snackbar.make(b.getRoot(), "Could not save profile: " + e.getMessage(),
                                                    Snackbar.LENGTH_LONG).show();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(b.getRoot(), "Sign up failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        b.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        b.btnRegister.setEnabled(!loading);
    }
}

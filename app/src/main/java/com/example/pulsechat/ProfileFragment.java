package com.example.pulsechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pulsechat.databinding.FragmentProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding b;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentProfileBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        b.tvName.setText(me.getDisplayName());
        b.tvEmail.setText(me.getEmail());

        db.collection("users").document(me.getUid()).get()
                .addOnSuccessListener(d -> {
                    if (b == null) return;
                    b.tvStatus.setText(d.getString("status"));
                    Long a = d.getLong("avatarId");
                    if (a != null) b.ivAvatar.setImageResource(Utils.avatarRes(a.intValue()));
                });

        View.OnClickListener changeAvatar = v -> AvatarPicker.show(requireContext(), id ->
                db.collection("users").document(me.getUid()).update("avatarId", id)
                        .addOnSuccessListener(x -> {
                            if (b != null) b.ivAvatar.setImageResource(Utils.avatarRes(id));
                        })
                        .addOnFailureListener(e -> {
                            if (b != null) Snackbar.make(b.getRoot(), "Could not save: " + e.getMessage(),
                                    Snackbar.LENGTH_LONG).show();
                        }));
        b.ivAvatar.setOnClickListener(changeAvatar);
        b.tvChange.setOnClickListener(changeAvatar);

        b.btnEditStatus.setOnClickListener(v -> {
            EditText input = new EditText(requireContext());
            input.setText(b.tvStatus.getText());
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Edit status")
                    .setView(input)
                    .setPositiveButton("Save", (dlg, w) -> {
                        String s = input.getText().toString().trim();
                        if (s.isEmpty()) return;
                        db.collection("users").document(me.getUid()).update("status", s)
                                .addOnSuccessListener(x -> { if (b != null) b.tvStatus.setText(s); })
                                .addOnFailureListener(e -> {
                                    if (b != null) Snackbar.make(b.getRoot(), "Could not save: " + e.getMessage(),
                                            Snackbar.LENGTH_LONG).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        b.btnLogout.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Log out?")
                        .setMessage("You'll need to log in again.")
                        .setPositiveButton("Log out", (dlg, w) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent i = new Intent(requireContext(), LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        })
                        .setNegativeButton("Cancel", null)
                        .show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}

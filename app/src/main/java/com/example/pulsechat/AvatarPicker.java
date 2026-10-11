package com.example.pulsechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AvatarPicker {

    public interface Callback { void onPick(int avatarId); }

    public static void show(Context ctx, Callback cb) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_avatar_picker, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(ctx)
                .setTitle("Choose your avatar 🌸")
                .setView(v)
                .setNegativeButton("Cancel", null)
                .create();

        int[] ids = {R.id.av0, R.id.av1, R.id.av2, R.id.av3, R.id.av4};
        for (int i = 0; i < ids.length; i++) {
            final int avatarId = i;
            v.findViewById(ids[i]).setOnClickListener(x -> {
                cb.onPick(avatarId);
                dialog.dismiss();
            });
        }
        dialog.show();
    }
}

package com.example.pulsechat.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsechat.Utils;
import com.example.pulsechat.databinding.ItemUserBinding;
import com.example.pulsechat.models.UserModel;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface OnUserClick { void onClick(UserModel user); }

    private final List<UserModel> items = new ArrayList<>();
    private final OnUserClick listener;

    public UserAdapter(OnUserClick listener) { this.listener = listener; }

    public void submit(List<UserModel> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserModel u = items.get(position);
        h.b.ivAvatar.setImageResource(Utils.avatarRes(u.avatarId));
        h.b.tvName.setText(u.name);
        h.b.tvStatus.setText(u.status);
        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemUserBinding b;
        VH(ItemUserBinding b) { super(b.getRoot()); this.b = b; }
    }
}

package com.example.pulsechat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsechat.Utils;
import com.example.pulsechat.databinding.ItemChatBinding;
import com.example.pulsechat.models.ChatItem;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.VH> {

    public interface OnChatClick { void onClick(ChatItem chat); }

    private final List<ChatItem> items = new ArrayList<>();
    private final OnChatClick listener;

    public ChatListAdapter(OnChatClick listener) { this.listener = listener; }

    public void submit(List<ChatItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ChatItem c = items.get(position);
        h.b.tvAvatar.setText(Utils.initial(c.otherName));
        h.b.tvName.setText(c.otherName);
        h.b.tvLast.setText(c.lastMessage);
        h.b.tvTime.setText(Utils.listTime(c.lastTime));
        if (c.unread > 0) {
            h.b.tvBadge.setVisibility(View.VISIBLE);
            h.b.tvBadge.setText(c.unread > 99 ? "99+" : String.valueOf(c.unread));
        } else {
            h.b.tvBadge.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemChatBinding b;
        VH(ItemChatBinding b) { super(b.getRoot()); this.b = b; }
    }
}

package com.example.pulsechat.adapters;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsechat.R;
import com.example.pulsechat.Utils;
import com.example.pulsechat.databinding.ItemMessageBinding;
import com.example.pulsechat.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private final List<Message> items = new ArrayList<>();
    private final String myUid;

    public MessageAdapter(String myUid) { this.myUid = myUid; }

    public void submit(List<Message> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Message m = items.get(position);
        boolean mine = myUid.equals(m.senderId);

        h.b.tvText.setText(m.text);
        h.b.tvTime.setText(Utils.time(m.timestamp));

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) h.b.bubble.getLayoutParams();
        lp.gravity = mine ? Gravity.END : Gravity.START;
        h.b.bubble.setLayoutParams(lp);
        h.b.bubble.setBackgroundResource(mine ? R.drawable.bg_bubble_sent : R.drawable.bg_bubble_received);
        h.b.tvText.setTextColor(mine ? Color.WHITE : 0xFF1F1B2E);
        h.b.tvTime.setTextColor(mine ? 0xCCFFFFFF : 0xFF6F6A87);

        boolean showDate = position == items.size() - 1
                || !Utils.sameDay(m.timestamp, items.get(position + 1).timestamp);
        h.b.tvDate.setVisibility(showDate ? View.VISIBLE : View.GONE);
        if (showDate) h.b.tvDate.setText(Utils.dayLabel(m.timestamp));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemMessageBinding b;
        VH(ItemMessageBinding b) { super(b.getRoot()); this.b = b; }
    }
}

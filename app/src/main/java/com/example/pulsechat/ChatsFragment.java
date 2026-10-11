package com.example.pulsechat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pulsechat.adapters.ChatListAdapter;
import com.example.pulsechat.databinding.FragmentChatsBinding;
import com.example.pulsechat.models.ChatItem;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatsFragment extends Fragment {

    private FragmentChatsBinding b;
    private ChatListAdapter adapter;
    private ListenerRegistration reg;
    private final List<ChatItem> all = new ArrayList<>();
    private String myUid;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentChatsBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) return;
        myUid = me.getUid();

        String name = me.getDisplayName();
        String first = (name == null || name.trim().isEmpty()) ? "there" : name.trim().split(" ")[0];
        b.tvGreeting.setText("Hello, " + first + " 🌸");

        FirebaseFirestore.getInstance().collection("users").document(myUid).get()
                .addOnSuccessListener(d -> {
                    Long a = d.getLong("avatarId");
                    if (b != null && a != null) b.ivMe.setImageResource(Utils.avatarRes(a.intValue()));
                });

        adapter = new ChatListAdapter(chat -> {
            Intent i = new Intent(requireContext(), ChatActivity.class);
            i.putExtra("otherUid", chat.otherUid);
            i.putExtra("otherName", chat.otherName);
            i.putExtra("otherAvatar", chat.otherAvatar);
            startActivity(i);
        });
        b.rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.rv.setAdapter(adapter);

        b.etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            public void onTextChanged(CharSequence s, int a, int c, int d) {}
            public void afterTextChanged(Editable s) { applyFilter(); }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (myUid == null) return;
        b.progress.setVisibility(View.VISIBLE);

        reg = FirebaseFirestore.getInstance().collection("chats")
                .whereArrayContains("participants", myUid)
                .addSnapshotListener((snap, e) -> {
                    if (b == null) return;
                    b.progress.setVisibility(View.GONE);
                    if (e != null) {
                        Snackbar.make(b.getRoot(), "Could not load chats: " + e.getMessage(),
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    all.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String last = d.getString("lastMessage");
                        if (last == null || last.isEmpty()) continue;

                        List<String> parts = (List<String>) d.get("participants");
                        if (parts == null || parts.size() != 2) continue;
                        String other = parts.get(0).equals(myUid) ? parts.get(1) : parts.get(0);

                        ChatItem c = new ChatItem();
                        c.chatId = d.getId();
                        c.otherUid = other;
                        Map<String, String> names = (Map<String, String>) d.get("names");
                        c.otherName = names != null && names.get(other) != null ? names.get(other) : "User";
                        Map<String, Long> avatars = (Map<String, Long>) d.get("avatars");
                        c.otherAvatar = avatars != null && avatars.get(other) != null
                                ? avatars.get(other).intValue() : 0;
                        c.lastMessage = last;
                        c.lastTime = d.getDate("lastMessageTime",
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                        Map<String, Long> unread = (Map<String, Long>) d.get("unread");
                        c.unread = unread != null && unread.get(myUid) != null ? unread.get(myUid) : 0;
                        all.add(c);
                    }
                    Collections.sort(all, (x, y) -> {
                        Date a = x.lastTime, c2 = y.lastTime;
                        if (a == null && c2 == null) return 0;
                        if (a == null) return -1;
                        if (c2 == null) return 1;
                        return c2.compareTo(a);
                    });
                    applyFilter();
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (reg != null) { reg.remove(); reg = null; }
    }

    private void applyFilter() {
        if (b == null) return;
        String q = b.etSearch.getText().toString().trim().toLowerCase();
        List<ChatItem> shown = new ArrayList<>();
        for (ChatItem c : all) {
            if (q.isEmpty() || c.otherName.toLowerCase().contains(q)) shown.add(c);
        }
        adapter.submit(shown);
        b.tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}

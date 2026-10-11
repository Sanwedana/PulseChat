package com.example.pulsechat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsechat.adapters.MessageAdapter;
import com.example.pulsechat.databinding.ActivityChatBinding;
import com.example.pulsechat.models.Message;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private static final int PAGE = 30;

    private static final String[] EMOJIS = {
            "😀","😁","😂","🤣","😊","😍","🥰","😘","😎","🤩","🥳","😇",
            "🙂","😉","😋","😜","🤗","🤔","😴","😭","😢","😡","😱","🥺",
            "👍","👎","👏","🙌","🙏","💪","👋","🤝","✌️","🤞","👌","🫶",
            "❤️","💖","💕","💜","💙","💚","💛","🧡","🔥","✨","🎉","🎂",
            "🌈","⭐","☀️","🌙","🍕","🍔","🍟","🍦","☕","🎮","🎵","📚"
    };

    private ActivityChatBinding b;
    private FirebaseFirestore db;
    private String myUid, myName, otherUid, otherName, chatId;
    private int myAvatar = 0, otherAvatar = 0;
    private DocumentReference chatRef;
    private MessageAdapter adapter;
    private LinearLayoutManager lm;
    private ListenerRegistration reg;
    private int limit = PAGE;
    private boolean loadingMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        otherUid = getIntent().getStringExtra("otherUid");
        otherName = getIntent().getStringExtra("otherName");
        otherAvatar = getIntent().getIntExtra("otherAvatar", 0);
        if (me == null || otherUid == null) { finish(); return; }

        myUid = me.getUid();
        myName = me.getDisplayName() == null ? "User" : me.getDisplayName();
        db = FirebaseFirestore.getInstance();
        chatId = Utils.chatId(myUid, otherUid);
        chatRef = db.collection("chats").document(chatId);

        db.collection("users").document(myUid).get().addOnSuccessListener(d -> {
            Long a = d.getLong("avatarId");
            if (a != null) myAvatar = a.intValue();
        });

        b.tvName.setText(otherName);
        b.ivAvatar.setImageResource(Utils.avatarRes(otherAvatar));
        b.btnBack.setOnClickListener(v -> finish());

        adapter = new MessageAdapter(myUid);
        lm = new LinearLayoutManager(this);
        lm.setReverseLayout(true);
        b.rv.setLayoutManager(lm);
        b.rv.setAdapter(adapter);

        b.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                int count = adapter.getItemCount();
                if (!loadingMore && count >= limit
                        && lm.findLastVisibleItemPosition() == count - 1) {
                    loadingMore = true;
                    limit += PAGE;
                    listen();
                }
            }
        });

        b.btnSend.setOnClickListener(v -> send());

        // ---- emoji panel ----
        b.gridEmoji.setAdapter(new ArrayAdapter<>(this, R.layout.item_emoji, EMOJIS));
        b.gridEmoji.setOnItemClickListener((parent, view, pos, id) -> {
            int at = Math.max(b.etMessage.getSelectionStart(), 0);
            b.etMessage.getText().insert(at, EMOJIS[pos]);
        });
        b.btnEmoji.setOnClickListener(v -> {
            if (b.gridEmoji.getVisibility() == View.VISIBLE) {
                b.gridEmoji.setVisibility(View.GONE);
            } else {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(b.etMessage.getWindowToken(), 0);
                b.gridEmoji.setVisibility(View.VISIBLE);
            }
        });
        b.etMessage.setOnClickListener(v -> b.gridEmoji.setVisibility(View.GONE));

        // ---- calls ----
        b.btnCall.setOnClickListener(v -> startCall(false));
        b.btnVideo.setOnClickListener(v -> startCall(true));
        listen();
    }

    private void listen() {
        if (reg != null) reg.remove();
        reg = chatRef.collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener((snap, e) -> {
                    b.progress.setVisibility(View.GONE);
                    if (e != null) {
                        loadingMore = false;
                        Snackbar.make(b.getRoot(), "Could not load messages: " + e.getMessage(),
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    List<Message> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Message m = d.toObject(Message.class,
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                        if (m != null) list.add(m);
                    }
                    adapter.submit(list);
                    b.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    b.tvBanner.setVisibility(snap.getMetadata().isFromCache() && !Utils.isOnline(this)
                            ? View.VISIBLE : View.GONE);

                    if (!loadingMore && !list.isEmpty()) b.rv.scrollToPosition(0);
                    loadingMore = false;

                    markRead();
                });
    }

    private void markRead() {
        chatRef.update("unread." + myUid, 0);
    }

    private void send() {
        String text = b.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        if (text.length() > 2000) {
            Snackbar.make(b.getRoot(), "Message too long (max 2000 characters)", Snackbar.LENGTH_SHORT).show();
            return;
        }
        b.etMessage.setText("");
        sendText(text);
    }

    /**
     * Calls use a free Jitsi Meet room. We create a random room name, post the link in the chat
     * so the other person can tap it, and open it for ourselves.
     * Limitation: there is no ringing/push notification; the other person must open the chat.
     */
    private void startCall(boolean video) {
        if (!Utils.isOnline(this)) {
            Snackbar.make(b.getRoot(), "You need an internet connection to start a call", Snackbar.LENGTH_LONG).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(video ? "Start video call?" : "Start voice call?")
                .setMessage("A join link will be sent to " + otherName + " in this chat.")
                .setPositiveButton("Call", (d, w) -> {
                    String room = "PulseChat-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                    String url = "https://meet.jit.si/" + room + (video ? "" : "#config.startWithVideoMuted=true");
                    sendText((video ? "📹 Video call" : "📞 Voice call") + " - tap to join:\n" + url);
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception e) {
                        Snackbar.make(b.getRoot(), "Could not open the call link", Snackbar.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendText(String text) {
        if (!Utils.isOnline(this)) {
            Snackbar.make(b.getRoot(), "You're offline. The message will be sent when you reconnect.",
                    Snackbar.LENGTH_LONG).show();
        }

        WriteBatch batch = db.batch();

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", myUid);
        msg.put("text", text);
        msg.put("timestamp", FieldValue.serverTimestamp());
        batch.set(chatRef.collection("messages").document(), msg);

        Map<String, String> names = new HashMap<>();
        names.put(myUid, myName);
        names.put(otherUid, otherName);

        Map<String, Integer> avatars = new HashMap<>();
        avatars.put(myUid, myAvatar);
        avatars.put(otherUid, otherAvatar);

        Map<String, Object> unread = new HashMap<>();
        unread.put(otherUid, FieldValue.increment(1));

        Map<String, Object> chat = new HashMap<>();
        chat.put("participants", Arrays.asList(myUid, otherUid));
        chat.put("names", names);
        chat.put("avatars", avatars);
        chat.put("lastMessage", text);
        chat.put("lastSenderId", myUid);
        chat.put("lastMessageTime", FieldValue.serverTimestamp());
        chat.put("unread", unread);
        batch.set(chatRef, chat, SetOptions.merge());

        batch.commit().addOnFailureListener(e ->
                Snackbar.make(b.getRoot(), "Message failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}

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

import com.example.pulsechat.adapters.UserAdapter;
import com.example.pulsechat.databinding.FragmentUsersBinding;
import com.example.pulsechat.models.UserModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private FragmentUsersBinding b;
    private UserAdapter adapter;
    private String myUid;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentUsersBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        myUid = FirebaseAuth.getInstance().getUid();
        adapter = new UserAdapter(user -> {
            Intent i = new Intent(requireContext(), ChatActivity.class);
            i.putExtra("otherUid", user.uid);
            i.putExtra("otherName", user.name);
            i.putExtra("otherAvatar", user.avatarId);
            startActivity(i);
        });
        b.rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.rv.setAdapter(adapter);

        b.etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            public void onTextChanged(CharSequence s, int a, int c, int d) {}
            public void afterTextChanged(Editable s) { load(s.toString().trim().toLowerCase()); }
        });
        load("");
    }

    private void load(String q) {
        b.progress.setVisibility(View.VISIBLE);
        Query query = FirebaseFirestore.getInstance().collection("users").orderBy("nameLower");
        if (!q.isEmpty()) {
            query = query.startAt(q).endAt(q + "\uf8ff");
        }
        query.limit(50).get()
                .addOnSuccessListener(snap -> {
                    if (b == null) return;
                    List<UserModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        UserModel u = d.toObject(UserModel.class);
                        if (u.uid != null && !u.uid.equals(myUid)) list.add(u);
                    }
                    adapter.submit(list);
                    b.progress.setVisibility(View.GONE);
                    b.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (b == null) return;
                    b.progress.setVisibility(View.GONE);
                    Snackbar.make(b.getRoot(), "Could not load users: " + e.getMessage(),
                            Snackbar.LENGTH_LONG).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}

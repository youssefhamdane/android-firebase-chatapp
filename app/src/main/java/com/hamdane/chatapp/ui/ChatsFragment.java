package com.hamdane.chatapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hamdane.chatapp.Api;
import com.hamdane.chatapp.R;
import com.hamdane.chatapp.adapters.ChatsAdapter;
import com.hamdane.chatapp.listenner.Listener;

public class ChatsFragment extends Fragment {


    private RecyclerView recyclerView;

    private ChatsAdapter chatsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Api.readChatlist(new Listener() {
            @Override
            public void success() {
                chatsAdapter = new ChatsAdapter(getContext(), Api.listChatUsers, ChatsFragment.this);
                recyclerView.setAdapter(chatsAdapter);
            }
        });

        chatsAdapter = new ChatsAdapter(getContext(), Api.listUsers, ChatsFragment.this);
        recyclerView.setAdapter(chatsAdapter);

        return view;
    }

}
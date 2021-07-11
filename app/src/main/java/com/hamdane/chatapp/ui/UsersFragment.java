package com.hamdane.chatapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hamdane.chatapp.Api;
import com.hamdane.chatapp.R;
import com.hamdane.chatapp.adapters.UsersAdapter;
import com.hamdane.chatapp.listenner.Listener;

public class UsersFragment extends Fragment {
    private RecyclerView recyclerListUsers;
    private UsersAdapter adapter;
    private SearchView searchUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_users, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListUsers = (RecyclerView) layout.findViewById(R.id.list_friends);
        searchUsers = layout.findViewById(R.id.search_user);
        searchUsers.onActionViewExpanded();
        searchUsers.clearFocus();
        searchUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                Api.searchUsers(s.toLowerCase(), new Listener() {
                    @Override
                    public void failed() {

                    }

                    @Override
                    public void success() {
                        adapter = new UsersAdapter(getContext(), Api.listUsers, UsersFragment.this);
                        recyclerListUsers.setAdapter(adapter);
                    }

                    @Override
                    public boolean condition(Object arg) {
                        return false;
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Api.searchUsers(s.toLowerCase(), new Listener() {
                    @Override
                    public void failed() {

                    }

                    @Override
                    public void success() {
                        adapter = new UsersAdapter(getContext(), Api.listUsers, UsersFragment.this);
                        recyclerListUsers.setAdapter(adapter);
                    }

                    @Override
                    public boolean condition(Object arg) {
                        return false;
                    }
                });
                return false;
            }
        });
        recyclerListUsers.setLayoutManager(linearLayoutManager);
        adapter = new UsersAdapter(getContext(), Api.listUsers, this);
        recyclerListUsers.setAdapter(adapter);
        Api.readUsers( new Listener() {
            @Override
            public void success() {
                adapter = new UsersAdapter(getContext(), Api.listUsers, UsersFragment.this);
                recyclerListUsers.setAdapter(adapter);
            }

            @Override
            public boolean condition(Object arg) {
                return searchUsers.getQuery().equals("");
            }
        });
        return layout;
    }
}


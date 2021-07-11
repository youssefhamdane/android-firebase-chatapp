package com.hamdane.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hamdane.chatapp.listenner.Listener;
import com.hamdane.chatapp.model.Chat;
import com.hamdane.chatapp.model.Chatlist;
import com.hamdane.chatapp.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Api {
    public static final int RC_SIGN_IN = 1;
    public static FirebaseAuth mAuth;
    public static GoogleSignInClient mGoogleSignInClient;
    public static FirebaseUser currentUser;
    public static ArrayList<User> listUsers = new ArrayList<User>();
    public static ArrayList<User> listChatUsers = new ArrayList<User>();
    public static ArrayList<Chatlist> listChats = new ArrayList<Chatlist>();
    public static DatabaseReference seenReference;
    public static ValueEventListener seenListener;
    public static void initFirebase(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        mAuth = FirebaseAuth.getInstance();
    }

    public static void login(Activity context) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        context.startActivityForResult(signInIntent, Api.RC_SIGN_IN);
    }
    public static void logout(Context context, final Listener listener) {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener((Activity) context,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.success();
                    }
                });
    }
    public static FirebaseUser getCurrentUser() {
        Api.currentUser = mAuth.getCurrentUser();
        return Api.currentUser;
    }

    public static void getGoogleSignins(Context context, Intent data, final Listener listener) throws ApiException {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        // Google Sign In was successful, authenticate with Firebase
        final GoogleSignInAccount account = task.getResult(ApiException.class);

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Api.currentUser = mAuth.getCurrentUser();

                            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(Api.currentUser.getUid());

                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("id", Api.currentUser.getUid());
                                        hashMap.put("username", Api.currentUser.getDisplayName().toLowerCase());
                                        hashMap.put("email", Api.currentUser.getEmail());
                                        hashMap.put("avatar", "default");
                                        hashMap.put("status", "offline");

                                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    listener.success();
                                                }
                                            }
                                        });
                                    }
                                    else listener.success();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            listener.failed();
                        }
                    }
                });
    }



    public static void searchUsers(String search_string, final Listener listener) {
        FirebaseDatabase.getInstance().getReference("users").orderByChild("username")
                .startAt(search_string).endAt(search_string+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Api.listUsers.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            User user = snapshot.getValue(User.class);
                            assert user != null;
                            assert Api.currentUser != null;
                            if (!user.getId().equals(Api.currentUser.getUid())){
                                Api.listUsers.add(user);
                            }
                        }
                        listener.success();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
    public static void readUsers(final Listener listener) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (listener.condition(null)) {
                    Api.listUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        if (!user.getId().equals(Api.currentUser.getUid())) {
                            Api.listUsers.add(user);
                        }
                    }
                    listener.success();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static void readChatlist(final Listener listener) {
        FirebaseDatabase.getInstance().getReference("chatlist").child(Api.currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Api.listChats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    listChats.add(chatlist);
                }

                chatListUsers(listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static void chatListUsers(final Listener listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listChatUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : listChats){
                        if (user.getId().equals(chatlist.getId())){
                            listChatUsers.add(user);
                        }
                    }
                }
                listener.success();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void lastMessage(final String userid, final Listener listener) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getChildrenCount();
                Chat lastChat = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (Api.currentUser != null && chat != null) {
                        if (chat.getReceiver().equals(Api.currentUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(Api.currentUser.getUid())) {
                            lastChat = chat;
                        }
                    }
                }

                listener.value(lastChat);
                listener.success();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static void getUser(final String userid, final Listener listener) {
        FirebaseDatabase.getInstance().getReference("users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.setId(userid);
                listener.value(user);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void readMessages(final User receiver, final Listener listener) {
        FirebaseDatabase.getInstance().getReference("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Chat> chats = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(Api.getCurrentUser().getUid()) && chat.getSender().equals(receiver.getId()) ||
                            chat.getReceiver().equals(receiver.getId()) && chat.getSender().equals(Api.currentUser.getUid())){
                        chats.add(chat);
                    }
                    listener.value(chats);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void seenMessage(final User receiver, final Listener listener){
        Log.i("seenMessage","start");
        seenReference = FirebaseDatabase.getInstance().getReference("chats");
        ValueEventListener seenListener = seenReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(Api.currentUser.getUid()) && chat.getSender().equals(receiver.getId())){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                        listener.success();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static void removeSeenListener() {
        if(Api.seenReference != null && Api.seenListener != null)
            seenReference.removeEventListener(seenListener);
    }
    public static void sendMessage(final User receiver, String message,Listener listener) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Api.currentUser.getUid());
        hashMap.put("receiver", receiver.getId());
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("timestamp", ServerValue.TIMESTAMP);

        reference.child("chats").push().setValue(hashMap);


        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chatlist")
                .child(Api.currentUser.getUid())
                .child(receiver.getId());

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(receiver.getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("chatlist")
                .child(receiver.getId())
                .child(Api.currentUser.getUid());
        chatRefReceiver.child("id").setValue(Api.currentUser.getUid());

        listener.success();
    }

    public static void sendNotifiaction(User receiver, final String message, Listener listener){
        listener.success();
    }

    public static void changeStatus(String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        FirebaseDatabase.getInstance().getReference("users").child(Api.currentUser.getUid()).updateChildren(hashMap);
    }

    public static void updateAvatar(String avatar,Listener listener){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("avatar", avatar);
        FirebaseDatabase.getInstance().getReference("users").child(Api.currentUser.getUid()).updateChildren(hashMap);
        listener.success();
    }
}

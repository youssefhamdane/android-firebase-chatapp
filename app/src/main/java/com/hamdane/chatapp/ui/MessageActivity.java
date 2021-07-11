package com.hamdane.chatapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.hamdane.chatapp.Api;
import com.hamdane.chatapp.R;


import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hamdane.chatapp.adapters.MessageAdapter;
import com.hamdane.chatapp.listenner.Listener;
import com.hamdane.chatapp.model.Chat;
import com.hamdane.chatapp.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    TextView status;

    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    Intent intent;

    String userid;

    boolean notify = false;

    User receiver = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        status = findViewById(R.id.status);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        Api.getUser(userid, new Listener<User>() {
            @Override
            public void value(User user) {
                receiver = user;
                username.setText(receiver.getUsername());
                status.setText(receiver.getStatus());
                if (receiver.getAvatar().equals("default")) {
                    profile_image.setImageResource(R.drawable.ic_baseline_account_circle_24);
                } else {
                    byte[] decodedString = Base64.decode(receiver.getAvatar(), Base64.DEFAULT);
                    Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profile_image.setImageBitmap(src);
                }
                Api.readMessages(receiver, new Listener<List<Chat>>() {
                    @Override
                    public void value(List<Chat> chats) {
                        messageAdapter = new MessageAdapter(MessageActivity.this, chats, receiver.getAvatar());
                        recyclerView.setAdapter(messageAdapter);
                    }
                });

                Api.seenMessage(receiver, new Listener() {
                    @Override
                    public void success() {
                    }
                });
            }
        });


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                final String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    Api.sendMessage(receiver, msg, new Listener() {
                        @Override
                        public void success() {
                            if (notify) {
                                Api.sendNotifiaction(receiver, msg, new Listener() {
                                    @Override
                                    public void success() {
                                    }
                                });
                            }
                            notify = false;
                        }
                    });
                } else {
                    Toast.makeText(MessageActivity.this, "Vous ne pouvez pas envoyer un message vide", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Api.changeStatus("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Api.removeSeenListener();
        Api.changeStatus("offline");
        currentUser("none");
    }
}
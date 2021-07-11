package com.hamdane.chatapp.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.hamdane.chatapp.Api;
import com.hamdane.chatapp.R;
import com.hamdane.chatapp.listenner.Listener;
import com.hamdane.chatapp.model.Chat;
import com.hamdane.chatapp.model.User;
import com.hamdane.chatapp.ui.MessageActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatItemViewHolder> {

    private ArrayList<User> listUsers;
    private Fragment fragment;
    private Context context;


    public ChatsAdapter(Context context, ArrayList<User> listUsers, Fragment fragment) {

        this.context = context;
        this.fragment = fragment;
        this.listUsers = listUsers;
    }

    @Override
    public ChatItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_chat, viewGroup, false);
        return new ChatItemViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final ChatItemViewHolder viewHolder, final int position) {

        final String username = listUsers.get(position).username;
        final String id = listUsers.get(position).id;
        final String avatar = listUsers.get(position).avatar;
        //----------------------------
        viewHolder.txtName.setText(username);
        //----------------------------
        if (avatar.equals("default")) {
            ((ChatItemViewHolder) viewHolder).avatar.setImageResource(R.drawable.ic_baseline_account_circle_24);
        } else {
            byte[] decodedString = Base64.decode(avatar, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((ChatItemViewHolder) viewHolder).avatar.setImageBitmap(src);
        }
        Api.lastMessage(id, new Listener<Chat>() {
            @Override
            public void value(Chat lastChat) {
                if(lastChat != null) {
                    viewHolder.txtMessage.setText(lastChat.getMessage());
                    String time = (String) android.text.format.DateUtils.getRelativeTimeSpanString (lastChat.getTimestamp());
                    viewHolder.txtTime.setText(time);
                }
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", id);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }
}

class ChatItemViewHolder extends RecyclerView.ViewHolder{
    public CircleImageView avatar;
    public TextView txtName, txtTime, txtMessage;
    private Context context;

    ChatItemViewHolder(Context context, View itemView) {
        super(itemView);
        avatar = (CircleImageView) itemView.findViewById(R.id.user_avatar);
        txtName = (TextView) itemView.findViewById(R.id.user_name);
        txtTime = (TextView) itemView.findViewById(R.id.last_date);
        txtMessage = (TextView) itemView.findViewById(R.id.last_message);
        this.context = context;
    }

}
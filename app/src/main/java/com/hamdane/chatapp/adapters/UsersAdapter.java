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

import com.hamdane.chatapp.R;
import com.hamdane.chatapp.model.User;
import com.hamdane.chatapp.ui.MessageActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UserItemViewHolder> {

    private ArrayList<User> listUsers;
    private Fragment fragment;
    private Context context;


    public UsersAdapter(Context context, ArrayList<User> listUsers, Fragment fragment) {

        this.context = context;
        this.fragment = fragment;
        this.listUsers = listUsers;
    }

    @Override
    public UserItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_user, viewGroup, false);
        return new UserItemViewHolder(context, view);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(UserItemViewHolder viewHolder, final int position) {

        final String username = listUsers.get(position).username;
        final String id = listUsers.get(position).id;
        final String avatar = listUsers.get(position).avatar;
        //----------------------------
        viewHolder.txtName.setText(username);
        //----------------------------
        if (avatar.equals("default")) {
            ((UserItemViewHolder) viewHolder).avatar.setImageResource(R.drawable.ic_baseline_account_circle_24);
        } else {
            byte[] decodedString = Base64.decode(avatar, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((UserItemViewHolder) viewHolder).avatar.setImageBitmap(src);
        }

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

class UserItemViewHolder extends RecyclerView.ViewHolder{
    public CircleImageView avatar;
    public TextView txtName;
    private Context context;

    UserItemViewHolder(Context context, View itemView) {
        super(itemView);
        avatar = (CircleImageView) itemView.findViewById(R.id.user_avatar);
        txtName = (TextView) itemView.findViewById(R.id.user_name);
        this.context = context;
    }
}
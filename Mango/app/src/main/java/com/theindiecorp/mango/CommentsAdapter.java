package com.theindiecorp.mango;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.theindiecorp.mango.activity.HomeActivity;
import com.theindiecorp.mango.activity.ProfileViewActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theindiecorp.mango.data.Comment;

import java.util.ArrayList;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {

    private ArrayList<Comment> dataSet;

    Context context;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView name, comment;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.comment = itemView.findViewById(R.id.comment);
        }
    }


    public CommentsAdapter(ArrayList<Comment> data, Context context) {
        this.dataSet = data;
        this.context = context;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.dataSet = comments;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final int position = listPosition;
        String userId = dataSet.get(position).getUserId();
        //Get the user name of the commenter
        databaseReference.child("users").child(userId).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.getValue(String.class);
                holder.name.setText(displayName);
                holder.comment.setText(dataSet.get(position).getMessage());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

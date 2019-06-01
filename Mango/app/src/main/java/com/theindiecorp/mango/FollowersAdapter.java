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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theindiecorp.mango.activity.HomeActivity;
import com.theindiecorp.mango.activity.ProfileViewActivity;

import java.util.ArrayList;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.MyViewHolder>  {
    private ArrayList<String> dataSet;
    private String name;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    Context context;
    FrameLayout layout;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private CircularImageView profileImg;
        View view;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.title = itemView.findViewById(R.id.followed_user_item_name);
            this.profileImg = itemView.findViewById(R.id.followed_user_item_display_pic);
        }
    }

    public FollowersAdapter(ArrayList<String> data, Context context) {
        this.dataSet = data;
        this.context = context;
    }

    public void setIds(ArrayList<String> ids) {
        this.dataSet = ids;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.followed_users_item, parent, false);
        FollowersAdapter.MyViewHolder myViewHolder = new FollowersAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final String userId = dataSet.get(listPosition);

        // profile image reference
        StorageReference profileImageReference = storage.getReference().child("users/" + userId + "/images/profile_pic/profile_pic.jpeg");
        profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context.getApplicationContext())
                        .load(uri)
                        .into(holder.profileImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(HomeActivity.TAG, exception.getMessage());
            }
        });

        databaseReference.child("users").child(userId).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.title.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileViewActivity.class);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
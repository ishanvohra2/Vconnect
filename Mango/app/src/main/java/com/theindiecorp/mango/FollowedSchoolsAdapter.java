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
import com.theindiecorp.mango.activity.HomeActivity;
import com.theindiecorp.mango.activity.SchoolViewActivity;

import java.util.ArrayList;

public class FollowedSchoolsAdapter extends RecyclerView.Adapter<FollowedSchoolsAdapter.MyViewHolder> {
    private ArrayList<String> dataSet;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    Context context;
    FrameLayout layout;

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView schoolName,schoolAddress,followers;
        private ImageView schoolImage;
        View view;

        public MyViewHolder(View itemView){
            super(itemView);
            this.view = itemView;
            this.schoolAddress = itemView.findViewById(R.id.school_item_address);
            this.schoolName = itemView.findViewById(R.id.school_item_school_name);
            this.schoolImage = itemView.findViewById(R.id.school_item_display_pic);
            this.followers = itemView.findViewById(R.id.school_item_followers);
        }
    }

    public FollowedSchoolsAdapter(ArrayList<String> data, Context context) {
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
                .inflate(R.layout.followed_schools_item, parent, false);
        FollowedSchoolsAdapter.MyViewHolder myViewHolder = new FollowedSchoolsAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final String schoolId = dataSet.get(listPosition);

        StorageReference profileImageReference = storage.getReference().child("schools/" + schoolId + "/images/profile_pic/profile_pic.jpeg");
        profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context.getApplicationContext())
                        .load(uri)
                        .into(holder.schoolImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(HomeActivity.TAG, exception.getMessage());
            }
        });

        databaseReference.child("schools").child(schoolId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.schoolName.setText(dataSnapshot.child("name").getValue(String.class));
                holder.schoolAddress.setText(dataSnapshot.child("address").getValue(String.class));
                holder.followers.setText(dataSnapshot.child("followerCount").getValue(Integer.class) + " followers");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SchoolViewActivity.class);
                intent.putExtra("schoolId", schoolId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

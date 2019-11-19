package com.theindiecorp.vconnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theindiecorp.vconnect.activity.GroupViewActivity;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.data.Group;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SearchGroupAdapter extends RecyclerView.Adapter<SearchGroupAdapter.MyViewHolder> {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private Context context;
    private ArrayList<Group> dataSet;

    public int setGroups(ArrayList<Group> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView membersTv, postsTv, groupNamtTv;
        private Button followBtn;
        private ImageView profileImg;

        public MyViewHolder(View itemView){
            super(itemView);
            this.membersTv = itemView.findViewById(R.id.profile_view_followers_tv);
            this.postsTv = itemView.findViewById(R.id.profile_view_posts_count_tv);
            this.followBtn = itemView.findViewById(R.id.follow_btn);
            this.profileImg = itemView.findViewById(R.id.profile_img);
            this.groupNamtTv = itemView.findViewById(R.id.group_name);
        }
    }

    public SearchGroupAdapter( ArrayList<Group> dataSet, Context context){
        this.dataSet = dataSet;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_search_item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final Group group = dataSet.get(listPosition);

        holder.groupNamtTv.setText(group.getName());
        holder.membersTv.setText(group.getMembers().size() + "");

        Query query = databaseReference.child("events").orderByChild("groupId").equalTo(group.getId());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        count++;
                    }
                }
                holder.postsTv.setText(count + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(group.getUrl()!=null){
            StorageReference imageReference = storage.getReference().child(group.getUrl());
            imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context.getApplicationContext())
                            .load(uri)
                            .into(holder.profileImg);
                    holder.profileImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(HomeActivity.TAG, exception.getMessage());
                }
            });
        }

        holder.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(group.getMembers().contains(HomeActivity.userId)){
                    holder.followBtn.setText("Unfollow");
                    holder.followBtn.setBackground(context.getDrawable(R.drawable.button_background_stroke));
                    holder.followBtn.setTextColor(context.getResources().getColor(android.R.color.black));
                    group.getMembers().remove(HomeActivity.userId);
                    databaseReference.child("groups").child(group.getId()).child("members").setValue(group.getMembers());
                }
                else {
                    holder.followBtn.setText("Follow");
                    holder.followBtn.setBackground(context.getDrawable(R.drawable.button_round_background_green));
                    holder.followBtn.setTextColor(context.getResources().getColor(android.R.color.white));
                    group.getMembers().add(HomeActivity.userId);
                    databaseReference.child("groups").child(group.getId()).child("members").setValue(group.getMembers());
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, GroupViewActivity.class).putExtra("groupId",group.getId()));
            }
        });

        if(group.getMembers().contains(HomeActivity.userId)){
            holder.followBtn.setText("Unfollow");
            holder.followBtn.setBackground(context.getDrawable(R.drawable.button_background_stroke));
            holder.followBtn.setTextColor(context.getResources().getColor(android.R.color.black));
        }
        else {
            holder.followBtn.setText("Follow");
            holder.followBtn.setBackground(context.getDrawable(R.drawable.button_round_background_green));
            holder.followBtn.setTextColor(context.getResources().getColor(android.R.color.white));
        }

        if(group.getAdminId().equals(HomeActivity.userId)){
            holder.followBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

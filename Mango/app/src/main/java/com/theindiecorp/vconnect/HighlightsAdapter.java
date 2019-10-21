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
import com.theindiecorp.vconnect.activity.GroupViewActivity;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.data.Group;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class HighlightsAdapter extends RecyclerView.Adapter<HighlightsAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Group> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public int setHighlights(ArrayList<Group> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView title,content;
        private ImageView imageView;

        public MyViewHolder(View itemView){
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.content = itemView.findViewById(R.id.content);
            this.imageView = itemView.findViewById(R.id.image);
        }
    }

    public HighlightsAdapter(Context context, ArrayList<Group> dataSet){
        this.context = context;
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.highlights_item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final Group group = dataSet.get(listPosition);

        holder.title.setText(group.getName());

        if(group.getName() != "Add New Group"){
            databaseReference.child("groups").child(group.getId()).child("members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        ArrayList<String> members = new ArrayList<>();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            members.add(snapshot.getKey());
                        }
                        holder.content.setText(members.size() + "Members");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if(group.getUrl()!=null){
                final BlurTransformation bitmapTransform = new BlurTransformation();
                StorageReference imageReference = storage.getReference().child(group.getUrl());
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context.getApplicationContext())
                                .load(uri)
                                .transform(new BlurTransformation(9))
                                .into(holder.imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(HomeActivity.TAG, exception.getMessage());
                    }
                });
            }
        }
        else{
            holder.content.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, GroupViewActivity.class)
                                            .putExtra("groupId",group.getId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

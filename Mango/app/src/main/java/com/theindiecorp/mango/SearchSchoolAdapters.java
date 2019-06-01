package com.theindiecorp.mango;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.mango.activity.SchoolViewActivity;

import java.util.ArrayList;

public class SearchSchoolAdapters extends RecyclerView.Adapter<SearchSchoolAdapters.MyViewHolder> {
    private ArrayList<String> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    Context context;
    FrameLayout layout;
    private int followerCount = 0;


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView schoolName,followers;
        private Button followBtn;

        public MyViewHolder(View itemView){
            super(itemView);
            this.schoolName = itemView.findViewById(R.id.school_name);
            this.followBtn = itemView.findViewById(R.id.school_view_follow_btn);
            this.followers = itemView.findViewById(R.id.number_of_members_tv);
        }
    }

    public SearchSchoolAdapters(ArrayList<String> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    public void setIds(ArrayList<String> ids){
        this.dataSet = ids;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_school_item,parent,false);
        SearchSchoolAdapters.MyViewHolder myViewHolder = new SearchSchoolAdapters.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final String schoolId = dataSet.get(listPosition);

        databaseReference.child("schools").child(schoolId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.schoolName.setText(dataSnapshot.child("name").getValue(String.class));
                holder.followers.setText(dataSnapshot.child("followerCount").getValue(Integer.class) + "\n Members");
                followerCount = dataSnapshot.child("followerCount").getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SchoolViewActivity.class);
                intent.putExtra("schoolId",schoolId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}

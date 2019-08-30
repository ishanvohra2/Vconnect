package com.theindiecorp.vconnect.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.theindiecorp.vconnect.FollowersAdapter;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.mainFeedRecyclerViewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SchoolViewActivity extends AppCompatActivity {

    private Boolean followed;
    private int followerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_view);

        final String schoolId = getIntent().getStringExtra("schoolId");

        final TextView schoolName = findViewById(R.id.school_view_name_tv);
        final TextView schoolAddress = findViewById(R.id.school_view_address_tv);
        final TextView followers = findViewById(R.id.school_view_followers_tv);
        final Button followBtn = findViewById(R.id.school_view_follow_btn);
        final TextView description = findViewById(R.id.school_view_description_tv);
        RecyclerView recyclerView = findViewById(R.id.school_view_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView membersList = findViewById(R.id.school_view_members_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        ArrayList<Event> events = new ArrayList<>();
        final ArrayList<String> members = new ArrayList<>();

        final mainFeedRecyclerViewAdapter adapter = new mainFeedRecyclerViewAdapter(events, this);
        recyclerView.setAdapter(adapter);

        final FollowersAdapter followersAdapter = new FollowersAdapter(members,this);
        membersList.setAdapter(followersAdapter);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("schools").child(schoolId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                schoolName.setText(dataSnapshot.child("name").getValue(String.class));
                schoolAddress.setText(dataSnapshot.child("address").getValue(String.class));
               description.setText(dataSnapshot.child("description").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query = FirebaseDatabase.getInstance().getReference("events");
        query.orderByChild("venueId").equalTo(schoolId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Event> events = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    final Event event = eventSnapshot.getValue(Event.class);
                    event.setId(eventSnapshot.getKey());
                    events.add(event);
                }
                adapter.setEvents(events);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("schools").child(schoolId).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                members.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    members.add(snapshot.getKey());
                }
                followersAdapter.setIds(members);
                followersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("schools").child(schoolId).child("followers").child(HomeActivity.userId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    followBtn.setText("Followed");
                    followed = true;
                }
                else{
                    followBtn.setText("Follow");
                    followed = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("schools").child(schoolId).child("followerCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followerCount = dataSnapshot.getValue(Integer.class);
                followers.setText(followerCount + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(followed){
                    databaseReference.child("schools").child(schoolId).child("followers").child(HomeActivity.userId).removeValue();
                    databaseReference.child("schools").child(schoolId).child("followerCount").setValue(followerCount-1);
                }
                else{
                    databaseReference.child("schools").child(schoolId).child("followers").child(HomeActivity.userId).setValue(true);
                    databaseReference.child("schools").child(schoolId).child("followerCount").setValue(followerCount+1);
                }
            }
        });

    }
}

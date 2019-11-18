package com.theindiecorp.vconnect.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.data.Group;
import com.theindiecorp.vconnect.mainFeedRecyclerViewAdapter;

import java.util.ArrayList;

public class GroupViewActivity extends AppCompatActivity {

    String groupId;

    TextView groupNametV, groupDescriptionTv, membersTv, postsTv;
    Button editInfoBtn, joinGroupBtn, createPostBtn;
    ImageView profileImage;

    RecyclerView recyclerView;
    mainFeedRecyclerViewAdapter mainFeedAdapter;

    ArrayList<Event> events = new ArrayList<>();

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);

        groupId = getIntent().getStringExtra("groupId");

        groupNametV = findViewById(R.id.profile_display_name_tv);
        groupDescriptionTv = findViewById(R.id.profile_description_tv);
        membersTv = findViewById(R.id.profile_view_followers_tv);
        postsTv = findViewById(R.id.profile_view_posts_count_tv);
        editInfoBtn = findViewById(R.id.edit_info_btn);
        profileImage = findViewById(R.id.profile_photo);
        joinGroupBtn = findViewById(R.id.join_group_btn);
        createPostBtn = findViewById(R.id.post_btn);
        recyclerView = findViewById(R.id.group_view_posts_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mainFeedAdapter = new mainFeedRecyclerViewAdapter(new ArrayList<Event>(),this);
        recyclerView.setAdapter(mainFeedAdapter);

        databaseReference.child("groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                groupNametV.setText(group.getName());
                groupDescriptionTv.setText(group.getGroupDescription());
                membersTv.setText(group.getMembers().size() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query = databaseReference.child("events").orderByChild("groupId").equalTo(groupId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    events = new ArrayList<>();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Event event = snapshot.getValue(Event.class);
                        event.setId(snapshot.getKey());
                        events.add(event);
                    }
                    mainFeedAdapter.setEvents(events);
                    mainFeedAdapter.notifyDataSetChanged();
                    postsTv.setText(events.size() + "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

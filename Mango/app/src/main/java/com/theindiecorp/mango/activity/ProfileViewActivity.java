package com.theindiecorp.mango.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.theindiecorp.mango.R;
import com.theindiecorp.mango.data.Event;
import com.theindiecorp.mango.data.User;
import com.theindiecorp.mango.mainFeedRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileViewActivity extends AppCompatActivity {

    private TextView name,description,followers,postCount;
    private User user = new User();
    private Boolean followed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        name = findViewById(R.id.profile_display_name_tv);
        description = findViewById(R.id.profile_description_tv);
        followers = findViewById(R.id.profile_view_followers_tv);
        postCount = findViewById(R.id.profile_view_posts_count_tv);
        final Button followBtn = findViewById(R.id.profile_view_follow_btn);
        final Button messageBtn = findViewById(R.id.profile_view_message_btn);

        RecyclerView recyclerView = findViewById(R.id.profile_view_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Event> events = new ArrayList<>();

        final mainFeedRecyclerViewAdapter adapter = new mainFeedRecyclerViewAdapter(events, this);
        recyclerView.setAdapter(adapter);

        final String userId = getIntent().getStringExtra("userId");

        if(userId.equals(HomeActivity.userId)){
            followBtn.setVisibility(View.GONE);
        }

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child("displayName").getValue(String.class));
                description.setText(dataSnapshot.child("bio").getValue(String.class));
                if(TextUtils.isEmpty(description.getText())){
                    description.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query = FirebaseDatabase.getInstance().getReference("events");
        query.orderByChild("hostId").equalTo(userId).addValueEventListener(new ValueEventListener() {
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
                postCount.setText(events.size() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers")
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    followBtn.setText("Followed");
                    followed = true;
                    messageBtn.setVisibility(View.VISIBLE);
                }
                else{
                    followBtn.setText("Follow");
                    followed = false;
                    messageBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> followerList = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                     followerList.add(snapshot.getKey());
                }
                followers.setText(followerList.size() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(followed){
                    databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers")
                            .child(userId).removeValue();
                }
                else{
                    databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers")
                            .child(userId).setValue(true);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

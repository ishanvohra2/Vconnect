package com.theindiecorp.vconnect.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.data.User;
import com.theindiecorp.vconnect.mainFeedRecyclerViewAdapter;
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
    ArrayList<Event> events = new ArrayList<>();
    private CircularImageView profile_pic;

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
        profile_pic = findViewById(R.id.profile_photo);

        final LinearLayout privateProfileBox = findViewById(R.id.watermark);

        final RecyclerView recyclerView = findViewById(R.id.profile_view_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        linearLayoutManager.scrollToPosition(events.size() - 1);
        recyclerView.setLayoutManager(linearLayoutManager);

        final mainFeedRecyclerViewAdapter adapter = new mainFeedRecyclerViewAdapter(events, this);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String userIdFromIntent = "";
        if(intent.hasExtra("intentType")){
            if(intent.getStringExtra("intentType").equals("startActivityFromNotification")){
                userIdFromIntent = intent.getStringExtra("link");
            }
            else{
                userIdFromIntent = intent.getStringExtra("userId");
            }
        }
        else{
            userIdFromIntent = intent.getStringExtra("userId");
        }
        final String userId = userIdFromIntent;
        Log.e("userId: ", userIdFromIntent);

        if(userId.equals(HomeActivity.userId)){
            followBtn.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            privateProfileBox.setVisibility(View.GONE);
        }

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
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

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers")
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    followBtn.setText("Followed");
                    followed = true;
                    messageBtn.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    privateProfileBox.setVisibility(View.GONE);
                }
                else{
                    followBtn.setText("Follow");
                    followed = false;
                    messageBtn.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    privateProfileBox.setVisibility(View.VISIBLE);
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
                    if(!dataSnapshot.child("isPrivate").exists()){
                        events.add(event);
                    }
                }
                adapter.setEvents(events);
                adapter.notifyDataSetChanged();
                postCount.setText(events.size() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference  followerReference = FirebaseDatabase.getInstance().getReference();
        followerReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int followerCountInteger = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.child("followers").exists()){
                        for (DataSnapshot followingSnapshot : snapshot.child("followers").getChildren()) {
                            Log.e("key", followingSnapshot.getKey());
                            if(followingSnapshot.getKey().equals(userId)){
                                followerCountInteger++;
                            }
                        }
                    }
                }
                followers.setText(String.valueOf(followerCountInteger));
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
                    recyclerView.setVisibility(View.GONE);
                    privateProfileBox.setVisibility(View.VISIBLE);
                }
                else{
                    databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers")
                            .child(userId).setValue(true);
                    recyclerView.setVisibility(View.VISIBLE);
                    privateProfileBox.setVisibility(View.GONE);
                }
            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileViewActivity.this, MessageActivity.class)
                        .putExtra("userid",userId));
            }
        });

        final Context context = this;
        if (context != null) {
            String path = "users/" + userId + "/images/profile_pic/profile_pic.jpeg";
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(profile_pic);
                }
            });
        }


        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(false);
    }
}

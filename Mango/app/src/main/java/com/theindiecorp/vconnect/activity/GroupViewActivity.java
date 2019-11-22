package com.theindiecorp.vconnect.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.data.Group;
import com.theindiecorp.vconnect.mainFeedRecyclerViewAdapter;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class GroupViewActivity extends AppCompatActivity {

    String groupId;

    TextView groupNametV, groupDescriptionTv, membersTv, postsTv;
    Button editInfoBtn, joinGroupBtn, createPostBtn;
    ImageView profileImage;

    RecyclerView recyclerView;
    mainFeedRecyclerViewAdapter mainFeedAdapter;

    ArrayList<Event> events = new ArrayList<>();
    Group group = new Group();

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);

        groupId = getIntent().getStringExtra("groupId");
        HomeActivity.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
                group = dataSnapshot.getValue(Group.class);
                if(group!=null){
                    groupNametV.setText(group.getName());
                    groupDescriptionTv.setText(group.getGroupDescription());
                    membersTv.setText(group.getMembers().size() + "");
                }

                if(group.getUrl()!=null){
                    StorageReference imageReference = storage.getReference().child(group.getUrl());
                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(GroupViewActivity.this)
                                    .load(uri)
                                    .into(profileImage);
                            profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(HomeActivity.TAG, exception.getMessage());
                        }
                    });
                }

                if(group.getAdminId().equals(HomeActivity.userId)){
                    editInfoBtn.setVisibility(View.VISIBLE);
                    joinGroupBtn.setVisibility(View.GONE);
                }
                else
                    editInfoBtn.setVisibility(View.GONE);

                if(group.getMembers().contains(HomeActivity.userId)){
                    createPostBtn.setVisibility(View.VISIBLE);
                    joinGroupBtn.setText("Leave Group");
                    joinGroupBtn.setTextColor(getResources().getColor(android.R.color.black));
                    joinGroupBtn.setBackground(getDrawable(R.drawable.button_background_stroke));
                }
                else{
                    createPostBtn.setVisibility(View.GONE);
                    joinGroupBtn.setText("Join");
                    joinGroupBtn.setTextColor(getResources().getColor(android.R.color.white));
                    joinGroupBtn.setBackground(getDrawable(R.drawable.button_round_background_green));
                }
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

        createPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.groupId = groupId;
                HomeActivity.postType = "post";
                startActivity(new Intent(GroupViewActivity.this,HomeActivity.class));

            }
        });

        editInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupViewActivity.this,EditGroupInfoActivity.class).putExtra("groupId",groupId));
            }
        });

        joinGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(group.getMembers().contains(HomeActivity.userId)){
                    joinGroupBtn.setText("Leave");
                    joinGroupBtn.setBackground(getDrawable(R.drawable.button_background_stroke));
                    joinGroupBtn.setTextColor(getResources().getColor(android.R.color.black));
                }
                else {
                    joinGroupBtn.setText("Join");
                    joinGroupBtn.setBackground(getDrawable(R.drawable.button_round_background_green));
                    joinGroupBtn.setTextColor(getResources().getColor(android.R.color.white));
                }
            }
        });

    }
}

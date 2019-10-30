package com.theindiecorp.vconnect.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theindiecorp.vconnect.R;

public class GroupViewActivity extends AppCompatActivity {

    private String groupId;

    TextView groupNametV, groupDescriptionTv, membersTv, postsTv;
    Button editInfoBtn, joinGroupBtn, createPostBtn;
    ImageView profileImage;

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

        databaseReference.child("")

    }
}
